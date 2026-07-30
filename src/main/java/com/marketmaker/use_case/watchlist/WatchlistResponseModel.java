package com.marketmaker.use_case.watchlist;

import java.util.List;

public class WatchlistResponseModel {

    /** One quoted ticker in the watchlist. */
    public static class Row {
        private final String ticker;
        private final double price;

        public Row(String ticker, double price) {
            this.ticker = ticker;
            this.price = price;
        }

        public String getTicker() { return ticker; }
        public double getPrice() { return price; }
    }

    private final List<Row> rows;

    public WatchlistResponseModel(List<Row> rows) {
        this.rows = rows;
    }

    public List<Row> getRows() { return rows; }
}
