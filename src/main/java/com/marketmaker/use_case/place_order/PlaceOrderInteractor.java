package com.marketmaker.use_case.place_order;

import java.time.Instant;
import java.util.UUID;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.entities.Trade;
import com.marketmaker.price_feed.PriceFeed;

public class PlaceOrderInteractor implements PlaceOrderInputBoundary {
    private static final double STARTING_BALANCE = 100_000.0;

    private final AccountDAO accountDAO;
    private final PriceFeed priceFeed;
    private final PlaceOrderOutputBoundary presenter;

    public PlaceOrderInteractor(AccountDAO accountDAO, PriceFeed priceFeed,
                                 PlaceOrderOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.priceFeed = priceFeed;
        this.presenter = presenter;
    }

    @Override
    public void execute(PlaceOrderRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            account = new Account(request.getAccountId(), STARTING_BALANCE);
        }

        String ticker = request.getTicker();
        int quantity = request.getQuantity();
        Quote quote = priceFeed.getQuote(ticker);
        double price = quote.getPrice();
        Position existing = findPosition(account, ticker);

        Double realizedPnL = null;
        if (request.getSide() == Order.Side.BUY) {
            double cost = price * quantity;
            if (cost > account.getUserBalance()) {
                presenter.presentFailure("Insufficient buying power.");
                return;
            }
            account.editBalance(-cost);
            replacePosition(account, existing, mergeBuy(existing, ticker, quantity, price));
        } else {
            if (existing == null || existing.getShares() < quantity) {
                presenter.presentFailure("Not enough shares to sell.");
                return;
            }
            account.editBalance(price * quantity);
            realizedPnL = (price - existing.getAveragePrice()) * quantity;
            replacePosition(account, existing, mergeSell(existing, quantity));
        }

        Order order = new Order(UUID.randomUUID().toString(), ticker, request.getSide(),
                Order.Type.MARKET, quantity, null, Instant.now());
        order.fill(price, Instant.now());
        account.addOrder(order);
        account.addTrade(new Trade(UUID.randomUUID().toString(), ticker, request.getSide(),
                quantity, price, Instant.now(), realizedPnL));

        accountDAO.save(account);

        Position resulting = findPosition(account, ticker);
        int newShareCount = resulting == null ? 0 : resulting.getShares();
        presenter.presentSuccess(new PlaceOrderResponseModel(
                ticker, quantity, price, account.getUserBalance(), newShareCount));
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

    // Position has no in-place mutators, so "updating" a holding means swapping it
    // out for a freshly built one. Remove the old (if any), add the new (if any).
    private void replacePosition(Account account, Position existing, Position updated) {
        if (existing != null) {
            account.removePosition(existing);
        }
        if (updated != null) {
            account.addPosition(updated);
        }
    }
}
