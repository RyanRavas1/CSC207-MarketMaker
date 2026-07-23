package com.marketmaker.use_case.user_profile;

import java.util.ArrayList;
import java.util.List;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;
import com.marketmaker.price_feed.PriceFeed;

/** Reports cash, live-valued holdings, and total equity for an account. */
public class ViewProfileInteractor implements ViewProfileInputBoundary {
    private final AccountDAO accountDAO;
    private final PriceFeed priceFeed;
    private final ViewProfileOutputBoundary presenter;

    public ViewProfileInteractor(AccountDAO accountDAO, PriceFeed priceFeed,
                                  ViewProfileOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.priceFeed = priceFeed;
        this.presenter = presenter;
    }

    @Override
    public void execute(ViewProfileRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        List<ViewProfileResponseModel.Holding> holdings = new ArrayList<>();
        double totalEquity = account.getUserBalance();
        for (Position position : account.getHoldings()) {
            int shares = position.getShares();
            double averagePrice = position.getAveragePrice();
            double currentPrice = priceFeed.getQuote(position.getTicker()).getPrice();
            double marketValue = currentPrice * shares;
            double unrealizedPnL = (currentPrice - averagePrice) * shares;
            totalEquity += marketValue;
            holdings.add(new ViewProfileResponseModel.Holding(
                    position.getTicker(), shares, averagePrice, currentPrice, marketValue, unrealizedPnL));
        }

        presenter.presentProfile(new ViewProfileResponseModel(
                account.getUserName(), account.getUserBalance(), holdings, totalEquity));
    }
}
