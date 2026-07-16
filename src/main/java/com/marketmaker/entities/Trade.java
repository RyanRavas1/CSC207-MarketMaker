package com.marketmaker.entities;

import java.time.Instant;

/** Immutable once created. Record of an order fill.*/
public class Trade {
    private final String id;
    private final String ticker;
    private final Order.Side side;
    private final int quantity;
    private final double price;
    private final Instant timestamp;
    private final Double realizedPnL; // null if this trade opened/added to a position

    public Trade(String id, String ticker, Order.Side side, int quantity,
                 double price, Instant timestamp, Double realizedPnL) {
        this.id = id;
        this.ticker = ticker;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
        this.realizedPnL = realizedPnL;
    }

    public String getId() { return id; }
    public String getTicker() { return ticker; }
    public Order.Side getSide() { return side; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public Instant getTimestamp() { return timestamp; }
    public Double getRealizedPnL() { return realizedPnL; }
}
