package com.marketmaker.use_case.view_positions;

import java.util.ArrayList;
import java.util.List;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.search_ticker.TickerDataAccessInterface;

/** Displays current holdings with share count, average cost, and live unrealized P/L. */
public class ViewPositionsInteractor implements ViewPositionsInputBoundary {
    private final AccountDAO accountDAO;
    private final TickerDataAccessInterface quoteDataAccess;
    private final ViewPositionsOutputBoundary presenter;

    public ViewPositionsInteractor(AccountDAO accountDAO, TickerDataAccessInterface quoteDataAccess,
                                    ViewPositionsOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.quoteDataAccess = quoteDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(ViewPositionsRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        List<PositionView> views = new ArrayList<>();
        for (Position position : account.getHoldings()) {
            Quote quote = quoteDataAccess.fetchQuote(position.getTicker());
            // Fall back to average cost (zero unrealized P/L) if a live quote isn't available.
            double currentPrice = quote != null ? quote.getPrice() : position.getAveragePrice();
            double unrealizedPnL = (currentPrice - position.getAveragePrice()) * position.getShares();
            views.add(new PositionView(position.getTicker(), position.getShares(),
                    position.getAveragePrice(), currentPrice, unrealizedPnL));
        }

        presenter.presentSuccess(new ViewPositionsResponseModel(views));
    }
}
