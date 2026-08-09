package com.marketmaker;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DemoDataTest {
    @Test void suppliesCoherentDashboardSampleData() {
        assertAll(() -> assertEquals(4, DemoData.positions().getPositions().size()),
                () -> assertEquals(5, DemoData.orderHistory().getOrders().size()),
                () -> assertEquals(3, DemoData.orderHistory().getTrades().size()),
                () -> assertEquals(6, DemoData.chart().getCandles().size()),
                () -> assertEquals("AAPL", DemoData.chart().getTicker()),
                () -> assertEquals(9, DemoData.watchlist().size()),
                () -> assertTrue(DemoData.summary().getTotalEquity() > DemoData.summary().getCash()));
    }
}
