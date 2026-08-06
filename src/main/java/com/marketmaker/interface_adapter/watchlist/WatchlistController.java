package com.marketmaker.interface_adapter.watchlist;

import java.util.List;

import com.marketmaker.use_case.watchlist.WatchlistInputBoundary;
import com.marketmaker.use_case.watchlist.WatchlistRequestModel;

/** Converts what the view has on screen into a watchlist request. */
public class WatchlistController {
    private final WatchlistInputBoundary interactor;

    public WatchlistController(WatchlistInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void refresh(List<String> tickers) {
        interactor.execute(new WatchlistRequestModel(tickers));
    }
}
