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
import com.marketmaker.price_feed.PriceFeedException;

/** Fills a market order immediately at the live quoted price. */
public class PlaceOrderInteractor implements PlaceOrderInputBoundary {
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
        // A negative quantity inverts every sign below: a BUY would credit cash and a
        // SELL would hand out shares, so reject it before anything touches the balance.
        if (request.getQuantity() <= 0) {
            presenter.presentFailure("Quantity must be positive.");
            return;
        }

        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        String ticker = request.getTicker();
        int quantity = request.getQuantity();
        // Priced before anything is debited, so a feed outage leaves the account untouched.
        double price;
        try {
            Quote quote = priceFeed.getQuote(ticker);
            price = quote.getPrice();
        } catch (PriceFeedException exception) {
            presenter.presentFailure(exception.getMessage());
            return;
        }
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
        account.addTrade(new Trade(UUID.randomUUID().toString(), order.getId(), ticker, request.getSide(),
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

    private void replacePosition(Account account, Position existing, Position updated) {
        account.replacePosition(existing, updated);
    }
}
