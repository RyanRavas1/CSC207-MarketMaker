package com.marketmaker.use_case.view_watchlist;

import java.util.List;

public class WatchlistRequestModel {
    private final List<String> tickers;

    public WatchlistRequestModel(List<String> tickers) {
        this.tickers = tickers;
    }

    public List<String> getTickers() { return tickers; }
}
