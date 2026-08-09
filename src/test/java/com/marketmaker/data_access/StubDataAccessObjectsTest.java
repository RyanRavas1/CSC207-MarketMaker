package com.marketmaker.data_access;

import com.marketmaker.entities.Candle;
import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.order_history.OrderHistoryEntry;
import com.marketmaker.use_case.view_candlestick_chart.Resolution;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StubDataAccessObjectsTest {
    @Test void quoteStubReturnsKnownTickerInExpectedPriceRangeAndNullForUnknown() {
        StubQuoteDataAccessObject dataAccess = new StubQuoteDataAccessObject();
        Quote quote = dataAccess.fetchQuote("AAPL");
        assertAll(() -> assertEquals("AAPL", quote.getTicker()), () -> assertTrue(quote.getPrice() >= 231.5),
                () -> assertTrue(quote.getPrice() <= 233.5), () -> assertNotNull(quote.getTimestamp()));
        assertNull(dataAccess.fetchQuote("UNKNOWN"));
    }

    @Test void historicalStubReturnsThirtyChronologicalCandlesForKnownTicker() {
        StubHistoricalDataAccessObject dataAccess = new StubHistoricalDataAccessObject();
        List<Candle> candles = dataAccess.fetchCandles("AAPL", Resolution.FIVE_MINUTE);
        assertEquals(30, candles.size());
        assertEquals("5", candles.get(0).getInterval());
        assertEquals("AAPL", candles.get(0).getTicker());
        assertTrue(candles.get(0).getTimestamp().isBefore(candles.get(29).getTimestamp()));
        assertTrue(candles.stream().allMatch(c -> c.getHigh() >= Math.max(c.getOpen(), c.getClose()) && c.getLow() <= Math.min(c.getOpen(), c.getClose())));
        assertTrue(dataAccess.fetchCandles("UNKNOWN", Resolution.ONE_DAY).isEmpty());
    }

    @Test void hardcodedHistoryHasExpectedRepresentativeRows() {
        List<OrderHistoryEntry> history = new HardcodedOrderHistoryDataAccessObject().loadAll();
        assertEquals(5, history.size());
        assertAll(() -> assertEquals("AAPL", history.get(0).getSymbol()), () -> assertEquals("BUY", history.get(0).getSide()),
                () -> assertEquals("Filled", history.get(0).getFillStatus()), () -> assertEquals("TSLA", history.get(4).getSymbol()));
    }
}
