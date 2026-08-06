package com.marketmaker.use_case.watchlist;

// Implemented by the presenter/UI layer, not by the interactor.
public interface WatchlistOutputBoundary {
    void presentWatchlist(WatchlistResponseModel response);
    void presentFailure(String errorMessage); // e.g. empty ticker list
}
