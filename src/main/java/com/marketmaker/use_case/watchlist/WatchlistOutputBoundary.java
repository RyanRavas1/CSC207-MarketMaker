package com.marketmaker.use_case.watchlist;

public interface WatchlistOutputBoundary {
    void presentWatchlist(WatchlistResponseModel response);
    void presentFailure(String errorMessage);
}
