package com.marketmaker.interface_adapter;

import java.util.concurrent.Executor;

import com.marketmaker.use_case.add_to_watchlist.AddToWatchlistInputBoundary;
import com.marketmaker.use_case.add_to_watchlist.AddToWatchlistRequestModel;
import com.marketmaker.use_case.receive_live_quotes.ReceiveLiveQuoteUpdatesInputBoundary;
import com.marketmaker.use_case.remove_from_watchlist.RemoveFromWatchlistInputBoundary;
import com.marketmaker.use_case.remove_from_watchlist.RemoveFromWatchlistRequestModel;

/**
 * Turns what the watchlist panel has on screen into add and remove requests.
 *
 * <p>A watched ticker is also a subscribed one: what the user put on the list is exactly the
 * set of prices the app should be receiving, so the two move together.
 *
 * <p>Both directions run on the worker. Each of these use cases reads the account, changes it
 * and writes it back, so running one on the event dispatch thread while a refresh runs the
 * other on the worker lost whichever save landed first - an added ticker would vanish again a
 * moment later because an unrelated save carried a copy of the account taken before the add.
 */
public class WatchlistController {
    private final AddToWatchlistInputBoundary addInteractor;
    private final RemoveFromWatchlistInputBoundary removeInteractor;
    private final ReceiveLiveQuoteUpdatesInputBoundary liveQuotes;
    private final Executor worker;
    private final String accountId;
    private final Runnable afterChange;

    public WatchlistController(AddToWatchlistInputBoundary addInteractor,
                               RemoveFromWatchlistInputBoundary removeInteractor,
                               ReceiveLiveQuoteUpdatesInputBoundary liveQuotes, Executor worker,
                               String accountId, Runnable afterChange) {
        this.addInteractor = addInteractor;
        this.removeInteractor = removeInteractor;
        this.liveQuotes = liveQuotes;
        this.worker = worker;
        this.accountId = accountId;
        this.afterChange = afterChange;
    }

    /**
     * Adds a ticker to the watchlist.
     *
     * <p>The symbol is not checked against the market first. That check was a quote call of
     * its own, and the feed answers a rate limit and an unknown symbol identically, so a
     * perfectly good ticker was silently refused whenever the quota ran short. A typo now
     * lands on the list showing no price, which the footer says out loud, and the user takes
     * it off with Remove.
     */
    public void add(String ticker) {
        String symbol = normalise(ticker);
        if (symbol.isEmpty()) {
            return;
        }
        worker.execute(() -> {
            addInteractor.execute(new AddToWatchlistRequestModel(accountId, symbol));
            liveQuotes.subscribe(symbol);
            afterChange.run();
        });
    }

    public void remove(String ticker) {
        String symbol = normalise(ticker);
        if (symbol.isEmpty()) {
            return;
        }
        worker.execute(() -> {
            removeInteractor.execute(new RemoveFromWatchlistRequestModel(accountId, symbol));
            liveQuotes.unsubscribe(symbol);
            afterChange.run();
        });
    }

    // Tickers are upper case everywhere else, so a lower-case entry still finds its stock.
    private String normalise(String ticker) {
        return ticker == null ? "" : ticker.trim().toUpperCase();
    }
}
