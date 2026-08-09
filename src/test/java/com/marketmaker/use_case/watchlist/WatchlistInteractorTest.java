package com.marketmaker.use_case.watchlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.marketmaker.entities.Quote;
import com.marketmaker.price_feed.PriceFeed;
import com.marketmaker.price_feed.PriceFeedException;

/** One quote per watched ticker, and the empty-list guard. */
class WatchlistInteractorTest {

    @Test
    void quotesEveryTicker() {
        PriceFeed feed = ticker -> new Quote(ticker, 42.0, Instant.now());
        WatchlistResponseModel[] captured = new WatchlistResponseModel[1];
        WatchlistOutputBoundary presenter = new WatchlistOutputBoundary() {
            @Override
            public void presentWatchlist(WatchlistResponseModel response) { captured[0] = response; }

            @Override
            public void presentFailure(String errorMessage) { throw new AssertionError(errorMessage); }
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
            @Override
            public void presentWatchlist(WatchlistResponseModel response) { }

            @Override
            public void presentFailure(String errorMessage) { failed[0] = true; }
        };

        new WatchlistInteractor(feed, presenter).execute(new WatchlistRequestModel(List.of()));

        assertTrue(failed[0]);
    }

    @Test
    void nullOrUnavailableTickersReportTheCorrectOutcome() {
        String[] failure = new String[1]; WatchlistResponseModel[] success = new WatchlistResponseModel[1];
        WatchlistOutputBoundary presenter = new WatchlistOutputBoundary() {
            public void presentWatchlist(WatchlistResponseModel response) { success[0] = response; }
            public void presentFailure(String errorMessage) { failure[0] = errorMessage; }
        };
        new WatchlistInteractor(ticker -> new Quote(ticker, 1, Instant.EPOCH), presenter).execute(new WatchlistRequestModel(null));
        assertEquals("Watchlist is empty.", failure[0]);
        new WatchlistInteractor(ticker -> { if (ticker.equals("BAD")) throw new PriceFeedException("not found"); return new Quote(ticker, 2, Instant.EPOCH); }, presenter)
                .execute(new WatchlistRequestModel(List.of("GOOD", "BAD")));
        assertEquals(1, success[0].getRows().size()); assertEquals(List.of("BAD"), success[0].getUnavailable());
        new WatchlistInteractor(ticker -> { throw new PriceFeedException("outage"); }, presenter).execute(new WatchlistRequestModel(List.of("BAD")));
        assertEquals("outage", failure[0]);
    }
}
