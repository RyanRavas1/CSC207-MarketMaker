package com.marketmaker.entities;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @Test void beginsPendingAndExposesConstructionFields() {
        Instant created = Instant.parse("2025-01-01T00:00:00Z"); Order order = new Order("id", "AAPL", Order.Side.SELL, Order.Type.LIMIT, 3, 20d, created);
        assertAll(() -> assertEquals("id", order.getId()), () -> assertEquals("AAPL", order.getTicker()), () -> assertEquals(Order.Side.SELL, order.getSide()),
                () -> assertEquals(Order.Type.LIMIT, order.getType()), () -> assertEquals(3, order.getQuantity()), () -> assertEquals(20, order.getLimitOrStopPrice()),
                () -> assertEquals(Order.Status.PENDING, order.getStatus()), () -> assertEquals(created, order.getCreatedAt()), () -> assertNull(order.getFilledAt()));
    }
    @Test void fillsAndCancelsOrder() {
        Order order = new Order("id", "AAPL", Order.Side.BUY, Order.Type.MARKET, 1, null, Instant.EPOCH); Instant filled = Instant.now(); order.fill(12.5, filled);
        assertEquals(Order.Status.FILLED, order.getStatus()); assertEquals(12.5, order.getFillPrice()); assertEquals(filled, order.getFilledAt());
        order.cancel(); assertEquals(Order.Status.CANCELED, order.getStatus());
    }
}
