package com.marketmaker.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class CandleTest {
    @Test void retainsOhlcvFields() {
        LocalDateTime time = LocalDateTime.of(2025, Month.JANUARY, 1, 9, 30);
        Candle candle = new Candle("AAPL", "5", 10, 13, 9, 12, 99, time);
        assertAll(() -> assertEquals("AAPL", candle.getTicker()), () -> assertEquals("5", candle.getInterval()),
                () -> assertEquals(10, candle.getOpen()), () -> assertEquals(13, candle.getHigh()), () -> assertEquals(9, candle.getLow()),
                () -> assertEquals(12, candle.getClose()), () -> assertEquals(99, candle.getVolume()), () -> assertEquals(time, candle.getTimestamp()));
    }
}
