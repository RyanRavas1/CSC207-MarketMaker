package com.marketmaker.price_feed;

import com.marketmaker.entities.Quote;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ReplayPriceFeedTest {
    @Test void returnsAndPersistsANudgedKnownPrice() {
        Map<String, Double> prices = new HashMap<>(); prices.put("AAPL", 100d);
        Quote quote = new ReplayPriceFeed(prices).getQuote("AAPL");
        assertAll(() -> assertEquals("AAPL", quote.getTicker()), () -> assertTrue(quote.getPrice() >= 95 && quote.getPrice() <= 105),
                () -> assertEquals(quote.getPrice(), prices.get("AAPL")), () -> assertNotNull(quote.getTimestamp()));
    }
    @Test void startsUnknownTickerAtDefaultPrice() {
        Map<String, Double> prices = new HashMap<>(); Quote quote = new ReplayPriceFeed(prices).getQuote("NEW");
        assertTrue(quote.getPrice() >= 95 && quote.getPrice() <= 105); assertTrue(prices.containsKey("NEW"));
    }
}
