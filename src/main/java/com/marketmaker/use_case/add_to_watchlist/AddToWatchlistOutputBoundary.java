package com.marketmaker.use_case.add_to_watchlist;

public interface AddToWatchlistOutputBoundary {
    void presentSuccess(AddToWatchlistResponseModel response);
    void presentFailure(String errorMessage);
}
