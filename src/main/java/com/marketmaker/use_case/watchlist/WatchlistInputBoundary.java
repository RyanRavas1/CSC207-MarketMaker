package com.marketmaker.use_case.watchlist;

/** Entry point called by controllers to run the watchlist use case. */
public interface WatchlistInputBoundary {
    void execute(WatchlistRequestModel request);
}
