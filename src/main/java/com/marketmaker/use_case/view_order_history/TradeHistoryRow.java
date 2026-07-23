package com.marketmaker.use_case.view_order_history;

import java.time.Instant;

import com.marketmaker.entities.Order;

public class TradeHistoryRow {
    private final String tradeId;
    private final String ticker;
    private final Order.Side side;
    private final int quantity;
    private final double price;
    private final Instant timestamp;
    private final Double realizedPnL; // null if this trade opened/added to a position

    public TradeHistoryRow(String tradeId, String ticker, Order.Side side, int quantity,
                            double price, Instant timestamp, Double realizedPnL) {
        this.tradeId = tradeId;
        this.ticker = ticker;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
        this.realizedPnL = realizedPnL;
    }

    public String getTradeId() { return tradeId; }
    public String getTicker() { return ticker; }
    public Order.Side getSide() { return side; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public Instant getTimestamp() { return timestamp; }
    public Double getRealizedPnL() { return realizedPnL; }
}
