package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.add_to_watchlist.AddToWatchlistInputBoundary;
import com.marketmaker.use_case.add_to_watchlist.AddToWatchlistRequestModel;
import com.marketmaker.use_case.remove_from_watchlist.RemoveFromWatchlistInputBoundary;
import com.marketmaker.use_case.remove_from_watchlist.RemoveFromWatchlistRequestModel;

/** Turns what the watchlist panel has on screen into add and remove requests. */
public class WatchlistController {
    private final AddToWatchlistInputBoundary addInteractor;
    private final RemoveFromWatchlistInputBoundary removeInteractor;
    private final String accountId;
    private final Runnable afterChange;

    public WatchlistController(AddToWatchlistInputBoundary addInteractor,
                               RemoveFromWatchlistInputBoundary removeInteractor,
                               String accountId, Runnable afterChange) {
        this.addInteractor = addInteractor;
        this.removeInteractor = removeInteractor;
        this.accountId = accountId;
        this.afterChange = afterChange;
    }

    public void add(String ticker) {
        String symbol = normalise(ticker);
        if (symbol.isEmpty()) {
            return;
        }
        addInteractor.execute(new AddToWatchlistRequestModel(accountId, symbol));
        afterChange.run();
    }

    public void remove(String ticker) {
        String symbol = normalise(ticker);
        if (symbol.isEmpty()) {
            return;
        }
        removeInteractor.execute(new RemoveFromWatchlistRequestModel(accountId, symbol));
        afterChange.run();
    }

    // Tickers are upper case everywhere else, so a lower-case entry still finds its stock.
    private String normalise(String ticker) {
        return ticker == null ? "" : ticker.trim().toUpperCase();
    }
}
