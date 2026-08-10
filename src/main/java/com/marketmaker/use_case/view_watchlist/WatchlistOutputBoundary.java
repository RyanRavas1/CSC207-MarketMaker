package com.marketmaker.use_case.view_watchlist;

public interface WatchlistOutputBoundary {
    void presentWatchlist(WatchlistResponseModel response);
    void presentFailure(String errorMessage);
}
