package com.marketmaker.price_feed;

import java.time.Instant;
import java.util.Map;

import com.marketmaker.entities.Quote;

/** Fake price feed used when the real market or Finnhub isn't available. */
public class ReplayPriceFeed implements PriceFeed {
    private final Map<String, Double> lastPrice;

    public ReplayPriceFeed(Map<String, Double> startingPrices) {
        this.lastPrice = startingPrices;
    }

    /** Nudges the ticker's last price by up to +/- $5. */
    @Override
    public Quote getQuote(String ticker) {
        double base = lastPrice.getOrDefault(ticker, 100.0);
        double next = base + (Math.random() - 0.5) * 10;
        lastPrice.put(ticker, next);
        return new Quote(ticker, next, Instant.now());
    }
}
