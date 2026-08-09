package com.marketmaker.use_case.watchlist;

import java.util.ArrayList;
import java.util.List;

import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.PriceFeed;
import com.marketmaker.use_case.PriceFeedException;

/**
 * Fetches a live quote for each watched ticker.
 *
 * <p>The response describes the whole watchlist, not just the part of it that priced: every
 * requested ticker gets a row, and one the feed could not price gets a row with no price. The
 * table is then a picture of what the user is watching rather than of what the network
 * happened to return in the last second.
 */
public class WatchlistInteractor implements WatchlistInputBoundary {
    private final PriceFeed priceFeed;
    private final WatchlistOutputBoundary presenter;

    public WatchlistInteractor(PriceFeed priceFeed, WatchlistOutputBoundary presenter) {
        this.priceFeed = priceFeed;
        this.presenter = presenter;
    }

    @Override
    public void execute(WatchlistRequestModel request) {
        // An empty watchlist is a result, not a failure: it is how the view learns that the
        // ticker the user just removed was the last one and the table should now be empty.
        List<String> tickers = request.getTickers() == null ? List.of() : request.getTickers();

        List<WatchlistResponseModel.Row> rows = new ArrayList<>(tickers.size());
        List<String> unavailable = new ArrayList<>();
        String lastError = "";
        for (String ticker : tickers) {
            // One PriceFeed call per ticker; no batching for now. A ticker that can't be
            // quoted still gets its row, priceless - it is on the watchlist either way, and
            // one bad symbol used to stop every other ticker from ever refreshing again.
            try {
                Quote quote = priceFeed.getQuote(ticker);
                rows.add(new WatchlistResponseModel.Row(ticker, quote.getPrice(), quote.getTimestamp()));
            } catch (PriceFeedException exception) {
                rows.add(new WatchlistResponseModel.Row(ticker, null, null));
                unavailable.add(ticker);
                lastError = exception.getMessage();
            }
        }

        presenter.presentWatchlist(new WatchlistResponseModel(rows, unavailable));

        // Nothing quoted at all is a feed problem - an outage or a rate limit - rather than a
        // handful of bad symbols. Reported after the rows so the reason the feed gave replaces
        // the "no price for everything" the response would otherwise put on the status line.
        if (!tickers.isEmpty() && unavailable.size() == tickers.size()) {
            presenter.presentFailure(lastError);
        }
    }
}
