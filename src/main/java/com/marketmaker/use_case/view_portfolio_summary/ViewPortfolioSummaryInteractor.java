package com.marketmaker.use_case.view_portfolio_summary;

import java.time.LocalDate;

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

        for (Position position : account.getHoldings()) {
            Quote quote = quoteDataAccess.fetchQuote(position.getTicker());
            double currentPrice = quote != null ? quote.getPrice() : position.getAveragePrice();
            holdingsValue += currentPrice * position.getShares();
        }

        double totalEquity = cash + holdingsValue;
        // Measured against what the account was worth at the day's first valuation, so it
        // reads zero on a flat day rather than showing every gain ever made on a holding.
        double dailyPnL = account.dailyPnL(LocalDate.now(), totalEquity);
        // The first valuation of the day sets that mark, which has to outlive the process.
        accountDAO.save(account);
        // No margin is modeled, so buying power is cash — less whatever resting buy orders
        // have already committed, which is money the user cannot spend twice.
        double buyingPower = account.buyingPower();

        presenter.presentSuccess(new ViewPortfolioSummaryResponseModel(cash, buyingPower, totalEquity, dailyPnL));
    }
}
