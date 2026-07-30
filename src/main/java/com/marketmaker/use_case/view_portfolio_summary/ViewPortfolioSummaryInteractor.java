package com.marketmaker.use_case.view_portfolio_summary;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.search_ticker.TickerDataAccessInterface;

/** Displays cash, buying power, total equity, and daily profit/loss. */
public class ViewPortfolioSummaryInteractor implements ViewPortfolioSummaryInputBoundary {
    private final AccountDAO accountDAO;
    private final TickerDataAccessInterface quoteDataAccess;
    private final ViewPortfolioSummaryOutputBoundary presenter;

    public ViewPortfolioSummaryInteractor(AccountDAO accountDAO, TickerDataAccessInterface quoteDataAccess,
                                           ViewPortfolioSummaryOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.quoteDataAccess = quoteDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(ViewPortfolioSummaryRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        double cash = account.getUserBalance();
        double holdingsValue = 0.0;
        // Daily P/L isn't tracked against a start-of-day snapshot yet, so this
        // is approximated as the unrealized P/L across current holdings.
        double dailyPnL = 0.0;

        for (Position position : account.getHoldings()) {
            Quote quote = quoteDataAccess.fetchQuote(position.getTicker());
            double currentPrice = quote != null ? quote.getPrice() : position.getAveragePrice();
            holdingsValue += currentPrice * position.getShares();
            dailyPnL += (currentPrice - position.getAveragePrice()) * position.getShares();
        }

        double totalEquity = cash + holdingsValue;
        // No margin is modeled, so buying power is just the available cash.
        double buyingPower = cash;

        presenter.presentSuccess(new ViewPortfolioSummaryResponseModel(cash, buyingPower, totalEquity, dailyPnL));
    }
}
