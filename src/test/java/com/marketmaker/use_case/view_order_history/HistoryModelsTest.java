package com.marketmaker.use_case.view_order_history;

import com.marketmaker.entities.Order;
import com.marketmaker.use_case.order_history.OrderHistoryEntry;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HistoryModelsTest {
    @Test void modelsRetainAllHistoryValues() {
        Instant time = Instant.EPOCH; OrderHistoryRow order = new OrderHistoryRow("o", "AAPL", Order.Side.BUY, Order.Type.LIMIT, 2, 10d, Order.Status.PENDING, time); TradeHistoryRow trade = new TradeHistoryRow("t", "AAPL", Order.Side.SELL, 2, 12d, time, 4d); OrderHistoryEntry entry = new OrderHistoryEntry("09:30", "AAPL", "BUY", "LIMIT", 2, "10", "Pending", "-");
        ViewOrderHistoryResponseModel response = new ViewOrderHistoryResponseModel(List.of(order), List.of(trade));
        assertAll(() -> assertEquals("o", order.getOrderId()), () -> assertEquals(10d, order.getLimitOrStopPrice()), () -> assertEquals("t", trade.getTradeId()), () -> assertEquals(4d, trade.getRealizedPnL()), () -> assertEquals("AAPL", entry.getSymbol()), () -> assertEquals("Pending", entry.getFillStatus()), () -> assertSame(order, response.getOrders().get(0)), () -> assertSame(trade, response.getTrades().get(0)));
    }
}
