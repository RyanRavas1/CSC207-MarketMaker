package com.marketmaker.use_case.remove_from_watchlist;

import com.marketmaker.use_case.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.use_case.receive_live_quotes.LiveQuoteDataAccessInterface;

/** Removes a ticker from the watchlist and stops tracking its live price. */
public class RemoveFromWatchlistInteractor implements RemoveFromWatchlistInputBoundary {
    private final AccountDAO accountDAO;
    private final LiveQuoteDataAccessInterface liveQuoteDataAccess;
    private final RemoveFromWatchlistOutputBoundary presenter;

    public RemoveFromWatchlistInteractor(AccountDAO accountDAO,
                                          LiveQuoteDataAccessInterface liveQuoteDataAccess,
                                          RemoveFromWatchlistOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.liveQuoteDataAccess = liveQuoteDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(RemoveFromWatchlistRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        String ticker = request.getTicker().toUpperCase();
        if (!account.getWatchlist().remove(ticker)) {
            presenter.presentFailure("Ticker not on watchlist.");
            return;
        }

        accountDAO.save(account);
        liveQuoteDataAccess.unsubscribe(ticker);

        presenter.presentSuccess(new RemoveFromWatchlistResponseModel(ticker, account.getWatchlist().getTickers()));
    }
}
