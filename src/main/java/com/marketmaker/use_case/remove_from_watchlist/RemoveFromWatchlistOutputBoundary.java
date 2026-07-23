package com.marketmaker.use_case.remove_from_watchlist;

public interface RemoveFromWatchlistOutputBoundary {
    void presentSuccess(RemoveFromWatchlistResponseModel response);
    void presentFailure(String errorMessage);
}
