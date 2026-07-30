package com.marketmaker.use_case.watchlist;

import java.util.ArrayList;
import java.util.List;

import com.marketmaker.entities.Quote;
import com.marketmaker.price_feed.PriceFeed;

/** Fetches a live quote for each watched ticker. */
public class WatchlistInteractor implements WatchlistInputBoundary {
    private final PriceFeed priceFeed;
    private final WatchlistOutputBoundary presenter;

    public WatchlistInteractor(PriceFeed priceFeed, WatchlistOutputBoundary presenter) {
        this.priceFeed = priceFeed;
        this.presenter = presenter;
    }

    @Override
    public void execute(WatchlistRequestModel request) {
        if (request.getTickers() == null || request.getTickers().isEmpty()) {
            presenter.presentFailure("Watchlist is empty.");
            return;
        }

        List<WatchlistResponseModel.Row> rows = new ArrayList<>();
        for (String ticker : request.getTickers()) {
            Quote quote = priceFeed.getQuote(ticker);
            rows.add(new WatchlistResponseModel.Row(ticker, quote.getPrice()));
        }
        presenter.presentWatchlist(new WatchlistResponseModel(rows));
    }
}
