package com.marketmaker.use_case.watchlist;

import java.util.ArrayList;
import java.util.List;

import com.marketmaker.entities.Quote;
import com.marketmaker.price_feed.PriceFeed;
import com.marketmaker.price_feed.PriceFeedException;

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
        // Nothing to quote, so fail fast instead of returning an empty response.
        if (request.getTickers() == null || request.getTickers().isEmpty()) {
            presenter.presentFailure("Watchlist is empty.");
            return;
        }

        List<WatchlistResponseModel.Row> rows = new ArrayList<>();
        List<String> unavailable = new ArrayList<>();
        String lastError = "";
        for (String ticker : request.getTickers()) {
            // One PriceFeed call per ticker; no batching for now. A ticker that can't be
            // quoted is set aside rather than abandoning the batch — one bad symbol used to
            // stop every other ticker from ever refreshing again.
            try {
                Quote quote = priceFeed.getQuote(ticker);
                rows.add(new WatchlistResponseModel.Row(ticker, quote.getPrice(), quote.getTimestamp()));
            } catch (PriceFeedException exception) {
                unavailable.add(ticker);
                lastError = exception.getMessage();
            }
        }

        // Nothing quoted at all is a feed problem (outage, rate limit), not a bad symbol.
        // Report it so the view keeps its last good prices instead of blanking the table.
        if (rows.isEmpty()) {
            presenter.presentFailure(lastError);
            return;
        }
        presenter.presentWatchlist(new WatchlistResponseModel(rows, unavailable));
    }
}
