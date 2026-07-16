package com.marketmaker.data_access;

import com.marketmaker.use_case.order_history.OrderHistoryDataAccessInterface;
import com.marketmaker.use_case.order_history.OrderHistoryEntry;

import java.util.List;

/**
 * Placeholder data source until real persistence exists.
 * Replace with a file-backed implementation of
 * {@link OrderHistoryDataAccessInterface} later.
 */
public class HardcodedOrderHistoryDataAccessObject implements OrderHistoryDataAccessInterface {
    @Override
    public List<OrderHistoryEntry> loadAll() {
        return List.of(
                new OrderHistoryEntry("09:31:04", "AAPL", "BUY",  "MARKET", 30, "—",     "Filled",    "—"),
                new OrderHistoryEntry("09:45:12", "NVDA", "BUY",  "LIMIT",  15, "118.00", "Filled",    "—"),
                new OrderHistoryEntry("10:02:41", "MSFT", "BUY",  "MARKET", 20, "—",     "Pending",   "—"),
                new OrderHistoryEntry("10:15:07", "AAPL", "SELL", "LIMIT",   5, "232.50", "Cancelled", "—"),
                new OrderHistoryEntry("11:04:22", "TSLA", "SELL", "STOP",   10, "240.00", "Filled",    "+152.30")
        );
    }
}
