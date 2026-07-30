package com.marketmaker.use_case.place_limit_stop_order;

import java.time.Instant;
import java.util.UUID;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Position;

/** Submits a limit or stop-loss order as pending. */
public class PlaceLimitStopOrderInteractor implements PlaceLimitStopOrderInputBoundary {
    private static final double STARTING_BALANCE = 100_000.0;

    private final AccountDAO accountDAO;
    private final PlaceLimitStopOrderOutputBoundary presenter;

    public PlaceLimitStopOrderInteractor(AccountDAO accountDAO, PlaceLimitStopOrderOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.presenter = presenter;
    }

    @Override
    public void execute(PlaceLimitStopOrderRequestModel request) {
        if (request.getType() == Order.Type.MARKET) {
            presenter.presentFailure("Use PlaceOrder for market orders.");
            return;
        }
        if (request.getType() == Order.Type.STOP_LOSS && request.getSide() != Order.Side.SELL) {
            presenter.presentFailure("Stop-loss orders can only be sell orders.");
            return;
        }
        if (request.getQuantity() <= 0 || request.getTriggerPrice() <= 0) {
            presenter.presentFailure("Quantity and trigger price must be positive.");
            return;
        }

        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            account = new Account(request.getAccountId(), STARTING_BALANCE);
        }

        // Checked against the trigger price now and it will be checked again for real at fill time.
        if (request.getSide() == Order.Side.BUY) {
            double worstCaseCost = request.getTriggerPrice() * request.getQuantity();
            if (worstCaseCost > account.getUserBalance()) {
                presenter.presentFailure("Insufficient buying power.");
                return;
            }
        } else {
            Position existing = findPosition(account, request.getTicker());
            if (existing == null || existing.getShares() < request.getQuantity()) {
                presenter.presentFailure("Not enough shares to sell.");
                return;
            }
        }

        Order order = new Order(UUID.randomUUID().toString(), request.getTicker(), request.getSide(),
                request.getType(), request.getQuantity(), request.getTriggerPrice(), Instant.now());
        account.addOrder(order);
        accountDAO.save(account);

        presenter.presentSuccess(new PlaceLimitStopOrderResponseModel(
                order.getId(), request.getTicker(), request.getType(), request.getTriggerPrice()));
    }

    private Position findPosition(Account account, String ticker) {
        for (Position position : account.getHoldings()) {
            if (position.getTicker().equals(ticker)) {
                return position;
            }
        }
        return null;
    }
}
