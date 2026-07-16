package com.marketmaker.use_case.order_history;

import java.util.List;

/**
 * Contract for anything that can supply order/trade history rows.
 * Owned by the use-case layer so the view and the data access layer
 * both depend on this interface, not on each other.
 */
public interface OrderHistoryDataAccessInterface {
    List<OrderHistoryEntry> loadAll();
}
