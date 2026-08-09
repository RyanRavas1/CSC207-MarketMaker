package com.marketmaker.entities;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class TradeTest {
    @Test void retainsImmutableTradeFieldsIncludingOptionalPnl() {
        Instant time = Instant.EPOCH; Trade trade = new Trade("id", "o-id", "AAPL", Order.Side.SELL, 2, 12.5, time, 5d); Trade opening = new Trade("open", "o-open", "AAPL", Order.Side.BUY, 1, 1, time, null);
        assertAll(() -> assertEquals("id", trade.getId()), () -> assertEquals("AAPL", trade.getTicker()), () -> assertEquals(Order.Side.SELL, trade.getSide()),
                () -> assertEquals(2, trade.getQuantity()), () -> assertEquals(12.5, trade.getPrice()), () -> assertEquals(time, trade.getTimestamp()), () -> assertEquals(5, trade.getRealizedPnL()), () -> assertNull(opening.getRealizedPnL()));
    }
}
