package com.marketmaker.use_case.remove_from_watchlist;

import java.util.List;

public class RemoveFromWatchlistResponseModel {
    private final String ticker;
    private final List<String> watchlistTickers;

    public RemoveFromWatchlistResponseModel(String ticker, List<String> watchlistTickers) {
        this.ticker = ticker;
        this.watchlistTickers = watchlistTickers;
    }

    public String getTicker() { return ticker; }
    public List<String> getWatchlistTickers() { return watchlistTickers; }
}
