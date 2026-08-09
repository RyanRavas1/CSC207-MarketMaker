package com.marketmaker.data_access;

import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Trade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonFileAccountDataAccessObjectTest {

    @Test
    void savingThenLoadingRoundTripsAllAccountData(@TempDir Path tempDir) {
        JsonFileAccountDataAccessObject dataAccess = new JsonFileAccountDataAccessObject(tempDir);

        Account account = new Account("wayne", 87_500.0);
        account.addPosition(new Position("AAPL", 10, 200.0));
        Order marketOrder = new Order("o1", "AAPL", Order.Side.BUY, Order.Type.MARKET, 10, null, Instant.EPOCH);
        marketOrder.fill(232.50, Instant.EPOCH);
        account.addOrder(marketOrder);
        Order pendingLimitOrder = new Order("o2", "MSFT", Order.Side.BUY, Order.Type.LIMIT, 5, 400.0, Instant.EPOCH);
        account.addOrder(pendingLimitOrder);
        account.addTrade(new Trade("t1", "o1", "AAPL", Order.Side.BUY, 10, 232.50, Instant.EPOCH, null));
        account.getWatchlist().add("TSLA");

        dataAccess.save(account);
        Account loaded = dataAccess.load("wayne");

        assertEquals("wayne", loaded.getUserName());
        assertEquals(87_500.0, loaded.getUserBalance());
        assertEquals(1, loaded.getHoldings().size());
        assertEquals("AAPL", loaded.getHoldings().get(0).getTicker());
        assertEquals(2, loaded.getPlacedOrders().size());
        assertEquals(Order.Status.PENDING, loaded.getPlacedOrders().get(1).getStatus());
        assertNull(loaded.getPlacedOrders().get(1).getFillPrice());
        assertEquals(1, loaded.getTradeLog().size());
        assertTrue(loaded.getWatchlist().contains("TSLA"));
    }

    @Test
    void loadingUnknownAccountReturnsNull(@TempDir Path tempDir) {
        JsonFileAccountDataAccessObject dataAccess = new JsonFileAccountDataAccessObject(tempDir);

        assertNull(dataAccess.load("ghost"));
    }
}
