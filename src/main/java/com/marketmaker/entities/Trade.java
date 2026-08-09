package com.marketmaker.entities;

import java.time.Instant;

/** Immutable once created. Record of an order fill.*/
public class Trade {
    private final String id;
    // The order this fill came from, so a history row can show what a given order realized.
    // Null for trades written before orders were linked, and for any fill with no order.
    private final String orderId;
    private final String ticker;
    private final Order.Side side;
    private final int quantity;
    private final double price;
    private final Instant timestamp;
    private final Double realizedPnL; // null if this trade opened/added to a position

    public Trade(String id, String orderId, String ticker, Order.Side side, int quantity,
                 double price, Instant timestamp, Double realizedPnL) {
        this.id = id;
        this.orderId = orderId;
        this.ticker = ticker;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
        this.realizedPnL = realizedPnL;
    }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getTicker() { return ticker; }
    public Order.Side getSide() { return side; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public Instant getTimestamp() { return timestamp; }
    public Double getRealizedPnL() { return realizedPnL; }
}
