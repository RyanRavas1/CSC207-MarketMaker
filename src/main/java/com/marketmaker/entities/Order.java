package com.marketmaker.entities;

import java.time.Instant;

public class Order {

    public enum Side {BUY, SELL}

    public enum Type {MARKET, LIMIT, STOP_LOSS}

    public enum Status {PENDING, FILLED, CANCELED}

    private final String id;
    private final String ticker;
    private final Side side;
    private final Type type;
    private final int quantity;
    private final Double limitOrStopPrice; // null for MARKET orders
    private Status status;
    private final Instant createdAt;
    private Instant filledAt;
    private Double fillPrice;

    public Order(String id, String ticker, Side side, Type type, int quantity,
                 Double limitOrStopPrice, Instant createdAt) {
        this.id = id;
        this.ticker = ticker;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.limitOrStopPrice = limitOrStopPrice;
        this.status = Status.PENDING;
        this.createdAt = createdAt;
    }

    // status transitions: PENDING -> FILLED / CANCELED only
    public void fill(double fillPrice, Instant filledAt) {
        this.fillPrice = fillPrice;
        this.filledAt = filledAt;
        this.status = Status.FILLED;
    }

    public void cancel() {
        this.status = Status.CANCELED;
    }

    public String getId() { return id; }
    public String getTicker() { return ticker; }
    public Side getSide() { return side; }
    public Type getType() { return type; }
    public int getQuantity() { return quantity; }
    public Double getLimitOrStopPrice() { return limitOrStopPrice; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getFilledAt() { return filledAt; }
    public Double getFillPrice() { return fillPrice; }
}
