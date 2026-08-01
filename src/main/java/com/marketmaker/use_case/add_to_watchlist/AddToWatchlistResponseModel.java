package com.marketmaker.use_case.add_to_watchlist;

import java.util.List;

public class AddToWatchlistResponseModel {
    private final String ticker;
    private final List<String> watchlistTickers;

    public AddToWatchlistResponseModel(String ticker, List<String> watchlistTickers) {
        this.ticker = ticker;
        this.watchlistTickers = watchlistTickers;
    }

    public String getTicker() { return ticker; }
    public List<String> getWatchlistTickers() { return watchlistTickers; }
}
