package com.marketmaker.entities;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class QuoteTest {
    @Test void retainsTickerPriceAndTimestamp() {
        Instant timestamp = Instant.EPOCH; Quote quote = new Quote("AAPL", 123.45, timestamp);
        assertAll(() -> assertEquals("AAPL", quote.getTicker()), () -> assertEquals(123.45, quote.getPrice()), () -> assertEquals(timestamp, quote.getTimestamp()));
    }
}
