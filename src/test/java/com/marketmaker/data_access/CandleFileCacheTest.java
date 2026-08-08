package com.marketmaker.data_access;

import com.marketmaker.entities.Candle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CandleFileCacheTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 7);

    private static List<Candle> twoDays() {
        return List.of(
                new Candle("AAPL", "D", 300.0, 302.0, 298.0, 301.5, 30_000_000.0,
                        LocalDateTime.of(2026, 8, 5, 0, 0)),
                new Candle("AAPL", "D", 314.34, 316.28, 309.23, 312.41, 46_139_901.0,
                        LocalDateTime.of(2026, 8, 6, 0, 0)));
    }

    @Test
    void readsBackWhatItWrote(@TempDir Path directory) {
        CandleFileCache cache = new CandleFileCache(directory);
        cache.write("AAPL", twoDays(), TODAY);

        List<Candle> read = cache.read("AAPL", TODAY);

        assertEquals(2, read.size());
        // Order matters: a chart drawn from a reversed series runs backwards.
        assertEquals(LocalDateTime.of(2026, 8, 5, 0, 0), read.get(0).getTimestamp());
        assertEquals(312.41, read.get(1).getClose());
        assertEquals(309.23, read.get(1).getLow());
        assertEquals(46_139_901.0, read.get(1).getVolume());
        assertEquals("AAPL", read.get(1).getTicker());
    }

    @Test
    void treatsYesterdaysFileAsStale(@TempDir Path directory) {
        CandleFileCache cache = new CandleFileCache(directory);
        cache.write("AAPL", twoDays(), TODAY.minusDays(1));

        // Today's close was still moving when that file was written, so it has to be refetched.
        assertTrue(cache.read("AAPL", TODAY).isEmpty());
    }

    @Test
    void reportsNothingForATickerNeverFetched(@TempDir Path directory) {
        assertTrue(new CandleFileCache(directory).read("NVDA", TODAY).isEmpty());
    }

    @Test
    void survivesACorruptFile(@TempDir Path directory) throws IOException {
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("AAPL.json"), "{\"fetchedOn\": truncated...");

        // Worth one wasted API call to refetch; not worth taking the chart panel down.
        assertTrue(new CandleFileCache(directory).read("AAPL", TODAY).isEmpty());
    }
}
