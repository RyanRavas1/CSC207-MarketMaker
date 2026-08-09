package com.marketmaker.interface_adapter;

import java.util.concurrent.Executor;

import com.marketmaker.use_case.add_to_watchlist.AddToWatchlistInputBoundary;
import com.marketmaker.use_case.add_to_watchlist.AddToWatchlistRequestModel;
import com.marketmaker.use_case.receive_live_quotes.ReceiveLiveQuoteUpdatesInputBoundary;
import com.marketmaker.use_case.search_ticker.SearchTickerInputBoundary;
import com.marketmaker.use_case.search_ticker.SearchTickerRequestModel;
import com.marketmaker.use_case.remove_from_watchlist.RemoveFromWatchlistInputBoundary;
import com.marketmaker.use_case.remove_from_watchlist.RemoveFromWatchlistRequestModel;

/**
 * Turns what the watchlist panel has on screen into add and remove requests.
 *
 * <p>A watched ticker is also a subscribed one: what the user put on the list is exactly the
 * set of prices the app should be receiving, so the two move together.
 */
public class WatchlistController {
    private final AddToWatchlistInputBoundary addInteractor;
    private final RemoveFromWatchlistInputBoundary removeInteractor;
    private final ReceiveLiveQuoteUpdatesInputBoundary liveQuotes;
    private final SearchTickerInputBoundary searchInteractor;
    private final TickerSearchPresenter search;
    private final Executor worker;
    private final String accountId;
    private final Runnable afterChange;

    public WatchlistController(AddToWatchlistInputBoundary addInteractor,
                               RemoveFromWatchlistInputBoundary removeInteractor,
                               ReceiveLiveQuoteUpdatesInputBoundary liveQuotes,
                               SearchTickerInputBoundary searchInteractor,
                               TickerSearchPresenter search, Executor worker,
                               String accountId, Runnable afterChange) {
        this.addInteractor = addInteractor;
        this.removeInteractor = removeInteractor;
        this.liveQuotes = liveQuotes;
        this.searchInteractor = searchInteractor;
        this.search = search;
        this.worker = worker;
        this.accountId = accountId;
        this.afterChange = afterChange;
    }

    /**
     * Adds a ticker, once the market has confirmed it exists.
     *
     * <p>The search runs first because the watchlist will happily hold a typo: it would sit
     * there priceless for ever, looking like an outage rather than a slip of the keyboard.
     */
    public void add(String ticker) {
        String symbol = normalise(ticker);
        if (symbol.isEmpty()) {
            return;
        }

        // Off the event dispatch thread: confirming the symbol is a call to the market, and
        // the window would sit frozen for the length of it.
        worker.execute(() -> {
            searchInteractor.execute(new SearchTickerRequestModel(symbol));
            if (!search.found()) {
                return;
            }

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
        removeInteractor.execute(new RemoveFromWatchlistRequestModel(accountId, symbol));
        liveQuotes.unsubscribe(symbol);
        afterChange.run();
    }

    // Tickers are upper case everywhere else, so a lower-case entry still finds its stock.
    private String normalise(String ticker) {
        return ticker == null ? "" : ticker.trim().toUpperCase();
    }
}
