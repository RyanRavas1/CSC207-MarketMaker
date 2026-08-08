package com.marketmaker.data_access;

import com.marketmaker.entities.Candle;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlphaVantageHistoricalDataAccessObjectTest {

    // Trimmed to three days, deliberately out of order: Alpha Vantage returns newest first,
    // and a chart drawn in that order runs backwards.
    private static final String THREE_DAYS = """
            {
              "Time Series (Daily)": {
                "2026-08-06": {"1. open": "314.34", "2. high": "316.28", "3. low": "309.23",
                               "4. close": "312.41", "5. volume": "46139901"},
                "2026-08-04": {"1. open": "300.00", "2. high": "302.00", "3. low": "298.00",
                               "4. close": "301.50", "5. volume": "30000000"},
                "2026-08-05": {"1. open": "305.00", "2. high": "310.00", "3. low": "304.00",
                               "4. close": "308.00", "5. volume": "35000000"}
              }
            }""";

    // parse() never touches the cache, so an unwritable path is fine and keeps the test off disk.
    private final AlphaVantageHistoricalDataAccessObject dataAccess =
            new AlphaVantageHistoricalDataAccessObject("test-key",
                    new CandleFileCache(Path.of("target", "unused-cache")));

    @Test
    void readsBarsOldestFirst() {
        List<Candle> candles = dataAccess.parse("AAPL", THREE_DAYS);

        assertEquals(3, candles.size());
        assertEquals(LocalDateTime.of(2026, 8, 4, 0, 0), candles.get(0).getTimestamp());
        assertEquals(LocalDateTime.of(2026, 8, 6, 0, 0), candles.get(2).getTimestamp());
    }

    @Test
    void readsEveryPriceOnABar() {
        Candle latest = dataAccess.parse("AAPL", THREE_DAYS).get(2);

        assertEquals("AAPL", latest.getTicker());
        assertEquals(314.34, latest.getOpen());
        assertEquals(316.28, latest.getHigh());
        assertEquals(309.23, latest.getLow());
        assertEquals(312.41, latest.getClose());
        assertEquals(46139901.0, latest.getVolume());
    }

    @Test
    void treatsAThrottledResponseAsNoData() {
        // The free tier answers 200 with prose when the quota runs out. Parsing that as a
        // series would put a made-up line on the chart.
        String throttled = "{\"Information\": \"Thank you for using Alpha Vantage!\"}";

        assertTrue(dataAccess.parse("AAPL", throttled).isEmpty());
    }
}
