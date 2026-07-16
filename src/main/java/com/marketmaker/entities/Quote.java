package com.marketmaker.entities;

import java.time.Instant;

/** One tick of the live price feed. */
public class Quote {
    private final String ticker;
    private final double price;
    private final Instant timestamp;

    public Quote(String ticker, double price, Instant timestamp) {
        this.ticker = ticker;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getTicker() { return ticker; }
    public double getPrice() { return price; }
    public Instant getTimestamp() { return timestamp; }
}
