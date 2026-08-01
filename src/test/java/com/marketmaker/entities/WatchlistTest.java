package com.marketmaker.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WatchlistTest {

    @Test
    void addsNewTicker() {
        Watchlist watchlist = new Watchlist();
        assertTrue(watchlist.add("AAPL"));
        assertTrue(watchlist.contains("AAPL"));
        assertEquals(1, watchlist.getTickers().size());
    }

    @Test
    void rejectsDuplicateTicker() {
        Watchlist watchlist = new Watchlist();
        watchlist.add("AAPL");
        assertFalse(watchlist.add("AAPL"));
        assertEquals(1, watchlist.getTickers().size());
    }

    @Test
    void removesTicker() {
        Watchlist watchlist = new Watchlist();
        watchlist.add("AAPL");
        assertTrue(watchlist.remove("AAPL"));
        assertFalse(watchlist.contains("AAPL"));
    }

    @Test
    void removingMissingTickerReturnsFalse() {
        Watchlist watchlist = new Watchlist();
        assertFalse(watchlist.remove("AAPL"));
    }
}
