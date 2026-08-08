package com.marketmaker.use_case.watchlist;

import java.time.Instant;
import java.util.List;

public class WatchlistResponseModel {

    /** One quoted ticker in the watchlist. */
    public static class Row {
        private final String ticker;
        private final double price;
        // When the market last traded at this price, not when we asked for it: after the
        // close those are hours apart, and the view says so rather than claiming it is live.
        private final Instant tradedAt;

        public Row(String ticker, double price, Instant tradedAt) {
            this.ticker = ticker;
            this.price = price;
            this.tradedAt = tradedAt;
        }

        public String getTicker() { return ticker; }
        public double getPrice() { return price; }
        public Instant getTradedAt() { return tradedAt; }
    }

    private final List<Row> rows; // one row per ticker that quoted, same order as the request
    private final List<String> unavailable; // tickers the feed had no price for

    public WatchlistResponseModel(List<Row> rows, List<String> unavailable) {
        this.rows = rows;
        this.unavailable = unavailable;
    }

    public List<Row> getRows() { return rows; }
    public List<String> getUnavailable() { return unavailable; }
}
