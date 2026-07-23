package com.marketmaker.use_case.receive_live_quotes;

import java.time.Instant;

public class LiveQuoteResponseModel {
    private final String ticker;
    private final double price;
    private final Instant timestamp;

    public LiveQuoteResponseModel(String ticker, double price, Instant timestamp) {
        this.ticker = ticker;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getTicker() { return ticker; }
    public double getPrice() { return price; }
    public Instant getTimestamp() { return timestamp; }
}
