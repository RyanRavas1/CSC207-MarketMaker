package com.marketmaker.use_case.watchlist;

import java.util.List;

public class WatchlistRequestModel {
    private final List<String> tickers; // symbols to quote, e.g. "AAPL"

    public WatchlistRequestModel(List<String> tickers) {
        this.tickers = tickers;
    }

    public List<String> getTickers() { return tickers; }
}
