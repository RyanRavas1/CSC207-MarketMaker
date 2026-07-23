package com.marketmaker.use_case.view_order_history;

import java.util.List;

public class ViewOrderHistoryResponseModel {
    private final List<OrderHistoryRow> orders;
    private final List<TradeHistoryRow> trades;

    public ViewOrderHistoryResponseModel(List<OrderHistoryRow> orders, List<TradeHistoryRow> trades) {
        this.orders = orders;
        this.trades = trades;
    }

    public List<OrderHistoryRow> getOrders() { return orders; }
    public List<TradeHistoryRow> getTrades() { return trades; }
}
