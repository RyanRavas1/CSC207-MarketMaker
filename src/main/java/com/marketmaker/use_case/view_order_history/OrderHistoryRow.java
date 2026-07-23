package com.marketmaker.use_case.view_order_history;

import java.time.Instant;

import com.marketmaker.entities.Order;

public class OrderHistoryRow {
    private final String orderId;
    private final String ticker;
    private final Order.Side side;
    private final Order.Type type;
    private final int quantity;
    private final Double limitOrStopPrice; // null for MARKET orders
    private final Order.Status status;
    private final Instant timestamp;

    public OrderHistoryRow(String orderId, String ticker, Order.Side side, Order.Type type,
                            int quantity, Double limitOrStopPrice, Order.Status status, Instant timestamp) {
        this.orderId = orderId;
        this.ticker = ticker;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.limitOrStopPrice = limitOrStopPrice;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getOrderId() { return orderId; }
    public String getTicker() { return ticker; }
    public Order.Side getSide() { return side; }
    public Order.Type getType() { return type; }
    public int getQuantity() { return quantity; }
    public Double getLimitOrStopPrice() { return limitOrStopPrice; }
    public Order.Status getStatus() { return status; }
    public Instant getTimestamp() { return timestamp; }
}
