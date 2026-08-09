package com.marketmaker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest {

    private static final int CALLS_PER_MINUTE = 40;

    @Test
    void keepsTheDefaultIntervalForAShortWatchlist() {
        assertEquals(10_000, Main.intervalFor(3));
        assertEquals(10_000, Main.intervalFor(6));
    }

    @Test
    void slowsDownRatherThanOverrunTheRateLimit() {
        // Seven tickers at one call each would be 42 a minute on a ten-second timer.
        assertTrue(Main.intervalFor(7) > 10_000);

        for (int tickers : new int[] {1, 7, 20, 60}) {
            double callsPerMinute = tickers * 60_000.0 / Main.intervalFor(tickers);
            assertTrue(callsPerMinute <= CALLS_PER_MINUTE,
                    tickers + " tickers would spend " + callsPerMinute + " calls a minute");
        }
    }
}
