package com.marketmaker.use_case.watchlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.marketmaker.entities.Quote;
import com.marketmaker.price_feed.PriceFeed;

class WatchlistInteractorTest {

    @Test
    void quotesEveryTicker() {
        PriceFeed feed = ticker -> new Quote(ticker, 42.0, Instant.now());
        WatchlistResponseModel[] captured = new WatchlistResponseModel[1];
        WatchlistOutputBoundary presenter = new WatchlistOutputBoundary() {
            public void presentWatchlist(WatchlistResponseModel r) { captured[0] = r; }
            public void presentFailure(String e) { throw new AssertionError(e); }
        };

        new WatchlistInteractor(feed, presenter)
                .execute(new WatchlistRequestModel(List.of("AAPL", "NVDA")));

        assertEquals(2, captured[0].getRows().size());
        assertEquals("AAPL", captured[0].getRows().get(0).getTicker());
        assertEquals(42.0, captured[0].getRows().get(0).getPrice());
    }

    @Test
    void emptyWatchlistFails() {
        PriceFeed feed = ticker -> new Quote(ticker, 1.0, Instant.now());
        boolean[] failed = {false};
        WatchlistOutputBoundary presenter = new WatchlistOutputBoundary() {
            public void presentWatchlist(WatchlistResponseModel r) { }
            public void presentFailure(String e) { failed[0] = true; }
        };

        new WatchlistInteractor(feed, presenter).execute(new WatchlistRequestModel(List.of()));

        assertTrue(failed[0]);
    }
}
