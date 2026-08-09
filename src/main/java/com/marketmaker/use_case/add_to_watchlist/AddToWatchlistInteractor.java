package com.marketmaker.use_case.add_to_watchlist;

import com.marketmaker.use_case.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.use_case.receive_live_quotes.LiveQuoteDataAccessInterface;
import com.marketmaker.use_case.receive_live_quotes.QuoteUpdateListener;

/** Adds a searched ticker to the user's watchlist and subscribes it to live price updates. */
public class AddToWatchlistInteractor implements AddToWatchlistInputBoundary {
    private final AccountDAO accountDAO;
    private final LiveQuoteDataAccessInterface liveQuoteDataAccess;
    private final QuoteUpdateListener quoteUpdateListener;
    private final AddToWatchlistOutputBoundary presenter;

    public AddToWatchlistInteractor(AccountDAO accountDAO,
                                     LiveQuoteDataAccessInterface liveQuoteDataAccess,
                                     QuoteUpdateListener quoteUpdateListener,
                                     AddToWatchlistOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.liveQuoteDataAccess = liveQuoteDataAccess;
        this.quoteUpdateListener = quoteUpdateListener;
        this.presenter = presenter;
    }

    @Override
    public void execute(AddToWatchlistRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        String ticker = request.getTicker().toUpperCase();
        if (!account.getWatchlist().add(ticker)) {
            presenter.presentFailure("Ticker already on watchlist.");
            return;
        }

        accountDAO.save(account);
        liveQuoteDataAccess.subscribe(ticker, quoteUpdateListener);

        presenter.presentSuccess(new AddToWatchlistResponseModel(ticker, account.getWatchlist().getTickers()));
    }
}
