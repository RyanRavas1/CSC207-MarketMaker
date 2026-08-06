package com.marketmaker.use_case.user_profile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;
import com.marketmaker.price_feed.PriceFeed;
import com.marketmaker.price_feed.PriceFeedException;

/** Reports cash, live-valued holdings, and total equity for an account. */
public class ViewProfileInteractor implements ViewProfileInputBoundary {
    private final AccountDAO accountDAO;
    private final PriceFeed priceFeed;
    private final ViewProfileOutputBoundary presenter;
    // "Today" is the user's day, not UTC's — injectable so a test can pin it.
    private final ZoneId zone;

    public ViewProfileInteractor(AccountDAO accountDAO, PriceFeed priceFeed,
                                 ViewProfileOutputBoundary presenter) {
        this(accountDAO, priceFeed, presenter, ZoneId.systemDefault());
    }

    public ViewProfileInteractor(AccountDAO accountDAO, PriceFeed priceFeed,
                                 ViewProfileOutputBoundary presenter, ZoneId zone) {
        this.accountDAO = accountDAO;
        this.priceFeed = priceFeed;
        this.presenter = presenter;
        this.zone = zone;
    }

    @Override
    public void execute(ViewProfileRequestModel request) {
        // accountDAO.get is a single keyed lookup, not a scan of every account.
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        List<ViewProfileResponseModel.Holding> holdings = new ArrayList<>();
        double totalEquity = account.getUserBalance(); // running total, starts at cash
        try {
            for (Position position : account.getHoldings()) {
                int shares = position.getShares();
                double averagePrice = position.getAveragePrice();
                double currentPrice = priceFeed.getQuote(position.getTicker()).getPrice();
                double marketValue = currentPrice * shares;
                double unrealizedPnL = (currentPrice - averagePrice) * shares; // gain/loss if sold now
                totalEquity += marketValue;
                holdings.add(new ViewProfileResponseModel.Holding(
                        position.getTicker(), shares, averagePrice, currentPrice, marketValue, unrealizedPnL));
            }
        } catch (PriceFeedException exception) {
            // Equity is only meaningful once every holding is priced, so report the outage
            // rather than showing a total that silently omits a position.
            presenter.presentFailure(exception.getMessage());
            return;
        }

        // Marking the day happens here, once the portfolio has been priced: the entity owns
        // the rule, and the use case owns the clock. Saving keeps the mark across restarts.
        LocalDate today = LocalDate.now(zone);
        double dailyPnL = account.dailyPnL(today, totalEquity);
        accountDAO.save(account);

        presenter.presentProfile(new ViewProfileResponseModel(
                account.getUserName(), account.getUserBalance(), holdings, totalEquity,
                account.getBuyingPower(), account.realizedPnLOn(today, zone), dailyPnL));
    }
}
