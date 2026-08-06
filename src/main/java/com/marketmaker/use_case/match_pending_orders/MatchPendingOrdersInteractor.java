package com.marketmaker.use_case.match_pending_orders;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.entities.Trade;

/** Fills pending limit/stop orders when an incoming price hits their trigger. */
public class MatchPendingOrdersInteractor implements MatchPendingOrdersInputBoundary {
    private final AccountDAO accountDAO;
    private final MatchPendingOrdersOutputBoundary presenter;

    public MatchPendingOrdersInteractor(AccountDAO accountDAO, MatchPendingOrdersOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.presenter = presenter;
    }

    @Override
    public void execute(MatchPendingOrdersRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            return;
        }

        Quote quote = request.getQuote();
        String ticker = quote.getTicker();
        double price = quote.getPrice();

        List<Order> candidates = new ArrayList<>();
        for (Order order : account.getPlacedOrders()) {
            if (order.getStatus() == Order.Status.PENDING
                    && order.getTicker().equals(ticker)
                    && triggered(order, price)) {
                candidates.add(order);
            }
        }

        boolean anyFilled = false;
        for (Order order : candidates) {
            // Check affordability/shares again in case another order used them up.
            if (order.getSide() == Order.Side.BUY) {
                if (price * order.getQuantity() > account.getUserBalance()) {
                    continue;
                }
            } else {
                Position existing = findPosition(account, order.getTicker());
                if (existing == null || existing.getShares() < order.getQuantity()) {
                    continue;
                }
            }
            fill(account, order, price);
            anyFilled = true;
        }

        if (anyFilled) {
            accountDAO.save(account);
        }
    }

    private boolean triggered(Order order, double price) {
        if (order.getType() == Order.Type.LIMIT) {
            return order.getSide() == Order.Side.BUY
                    ? price <= order.getLimitOrStopPrice()
                    : price >= order.getLimitOrStopPrice();
        }
        if (order.getType() == Order.Type.STOP_LOSS) {
            return price <= order.getLimitOrStopPrice();
        }
        return false;
    }

    private void fill(Account account, Order order, double price) {
        String ticker = order.getTicker();
        int quantity = order.getQuantity();
        Position existing = findPosition(account, ticker);

        Double realizedPnL = null;
        if (order.getSide() == Order.Side.BUY) {
            account.editBalance(-price * quantity);
            replacePosition(account, existing, mergeBuy(existing, ticker, quantity, price));
        } else {
            realizedPnL = (price - existing.getAveragePrice()) * quantity;
            account.editBalance(price * quantity);
            replacePosition(account, existing, mergeSell(existing, quantity));
        }

        order.fill(price, Instant.now());
        account.addTrade(new Trade(UUID.randomUUID().toString(), ticker, order.getSide(),
                quantity, price, Instant.now(), realizedPnL));

        Position resulting = findPosition(account, ticker);
        int newShareCount = resulting == null ? 0 : resulting.getShares();
        presenter.presentFill(new MatchPendingOrdersResponseModel(
                order.getId(), ticker, price, account.getUserBalance(), newShareCount));
    }

    private Position findPosition(Account account, String ticker) {
        for (Position position : account.getHoldings()) {
            if (position.getTicker().equals(ticker)) {
                return position;
            }
        }
        return null;
    }

    private Position mergeBuy(Position existing, String ticker, int quantity, double price) {
        if (existing == null) {
            return new Position(ticker, quantity, price);
        }
        int totalShares = existing.getShares() + quantity;
        double totalCost = existing.getShares() * existing.getAveragePrice() + quantity * price;
        return new Position(ticker, totalShares, totalCost / totalShares);
    }

    private Position mergeSell(Position existing, int quantity) {
        int remaining = existing.getShares() - quantity;
        return remaining == 0 ? null : new Position(existing.getTicker(), remaining, existing.getAveragePrice());
    }

    private void replacePosition(Account account, Position existing, Position updated) {
        account.replacePosition(existing, updated);
    }
}
