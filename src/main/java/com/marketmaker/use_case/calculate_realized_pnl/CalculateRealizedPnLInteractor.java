package com.marketmaker.use_case.calculate_realized_pnl;

import com.marketmaker.use_case.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;

/** Computes the profit or loss on a position when it is sold or reduced. Read-only: does not fill any order. */
public class CalculateRealizedPnLInteractor implements CalculateRealizedPnLInputBoundary {
    private final AccountDAO accountDAO;
    private final CalculateRealizedPnLOutputBoundary presenter;

    public CalculateRealizedPnLInteractor(AccountDAO accountDAO, CalculateRealizedPnLOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.presenter = presenter;
    }

    @Override
    public void execute(CalculateRealizedPnLRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        Position position = findPosition(account, request.getTicker());
        if (position == null) {
            presenter.presentFailure("No position in " + request.getTicker() + ".");
            return;
        }
        if (request.getQuantitySold() <= 0) {
            presenter.presentFailure("Quantity sold must be positive.");
            return;
        }
        if (request.getQuantitySold() > position.getShares()) {
            presenter.presentFailure("Cannot sell more shares than held.");
            return;
        }

        double realizedPnL = (request.getSalePrice() - position.getAveragePrice()) * request.getQuantitySold();
        presenter.presentSuccess(new CalculateRealizedPnLResponseModel(
                request.getTicker(), request.getQuantitySold(), realizedPnL));
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
