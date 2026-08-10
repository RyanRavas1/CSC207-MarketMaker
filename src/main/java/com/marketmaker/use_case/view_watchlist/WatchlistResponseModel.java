package com.marketmaker.use_case.view_watchlist;

import java.time.Instant;
import java.util.List;

public class WatchlistResponseModel {

    /**
     * One watched ticker, priced if the feed could price it.
     *
     * <p>Price and timestamp are nullable because a row exists for every ticker on the
     * watchlist, quoted or not. A quote is an HTTP call and calls fail; dropping the row
     * when one does made the table flicker, and left no way to tell a ticker the user
     * removed from a ticker whose call timed out.
     */
    public static class Row {
        private final String ticker;
        private final Double price;
        // When the market last traded at this price, not when we asked for it: after the
        // close those are hours apart, and the view says so rather than claiming it is live.
        private final Instant tradedAt;

        public Row(String ticker, Double price, Instant tradedAt) {
            this.ticker = ticker;
            this.price = price;
            this.tradedAt = tradedAt;
        }

        public String getTicker() { return ticker; }
        public Double getPrice() { return price; }
        public Instant getTradedAt() { return tradedAt; }
    }

    // One row per watched ticker, in watchlist order - this is the table, not just the part
    // of it that priced. A row with a null price is watched but unquoted.
    private final List<Row> rows;
    private final List<String> unavailable; // tickers the feed had no price for

    public WatchlistResponseModel(List<Row> rows, List<String> unavailable) {
        this.rows = rows;
        this.unavailable = unavailable;
    }

    public List<Row> getRows() { return rows; }
    public List<String> getUnavailable() { return unavailable; }
}
