package com.marketmaker.entities;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    @Test void updatesBalanceUsernameAndCollections() {
        Account account = new Account("ada", 100);
        Position position = new Position("AAPL", 2, 10);
        Order order = new Order("o", "AAPL", Order.Side.BUY, Order.Type.MARKET, 2, null, Instant.EPOCH);
        Trade trade = new Trade("t", "AAPL", Order.Side.BUY, 2, 10, Instant.EPOCH, null);
        assertEquals(125, account.editBalance(25)); assertEquals("grace", account.changeUsername("grace"));
        account.addPosition(position); account.addOrder(order); account.addTrade(trade);
        assertEquals(position, account.getHoldings().get(0)); assertEquals(order, account.getPlacedOrders().get(0)); assertEquals(trade, account.getTradeLog().get(0));
        account.removePosition(position); account.removeOrder(order);
        assertTrue(account.getHoldings().isEmpty()); assertTrue(account.getPlacedOrders().isEmpty());
    }

    @Test void replacesOrRemovesPositionsWithoutChangingOrder() {
        Account account = new Account("ada", 0); Position aapl = new Position("AAPL", 1, 10); Position msft = new Position("MSFT", 1, 20); account.addPosition(aapl); account.addPosition(msft);
        Position replacement = new Position("AAPL", 3, 12); account.replacePosition(aapl, replacement);
        assertSame(replacement, account.getHoldings().get(0)); assertSame(msft, account.getHoldings().get(1));
        account.replacePosition(replacement, null); assertEquals(1, account.getHoldings().size());
        account.replacePosition(null, replacement); assertEquals(replacement, account.getHoldings().get(1));
    }

    @Test void tracksDailyAndRealizedProfitAndLoss() {
        Account account = new Account("ada", 100); LocalDate day = LocalDate.of(2025, Month.JANUARY, 2);
        assertEquals(0, account.dailyPnL(day, 120)); assertEquals(5, account.dailyPnL(day, 125)); assertEquals(0, account.dailyPnL(day.plusDays(1), 130));
        account.addTrade(new Trade("today", "AAPL", Order.Side.SELL, 1, 1, Instant.parse("2025-01-02T12:00:00Z"), 4d));
        account.addTrade(new Trade("other", "AAPL", Order.Side.BUY, 1, 1, Instant.parse("2025-01-01T12:00:00Z"), 8d));
        account.addTrade(new Trade("open", "AAPL", Order.Side.BUY, 1, 1, Instant.parse("2025-01-02T12:00:00Z"), null));
        assertEquals(4, account.realizedPnLOn(day, ZoneId.of("UTC"))); assertEquals(100, account.getBuyingPower());
    }
}
