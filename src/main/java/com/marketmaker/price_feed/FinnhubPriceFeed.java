package com.marketmaker.price_feed;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import com.marketmaker.data_access.FinnhubApiClient;
import com.marketmaker.data_access.exceptions.FinnhubApiException;
import com.marketmaker.entities.Quote;

/** Live quotes from Finnhub's /quote endpoint. */
public class FinnhubPriceFeed implements PriceFeed {
    /**
     * The free tier allows 60 calls a minute, and the watchlist, the profile screen and the
     * order matcher all ask for the same tickers seconds apart. Serving a few-second-old quote
     * caps one ticker at 12 calls a minute no matter how many callers there are.
     * ponytail: fixed TTL, no eviction — bound the map if a watchlist ever gets long enough
     * to matter, or batch tickers into one request.
     */
    private static final Duration TTL = Duration.ofSeconds(5);

    private final FinnhubApiClient client;
    private final Map<String, Quote> cache = new ConcurrentHashMap<>();

    public FinnhubPriceFeed(FinnhubApiClient client) {
        this.client = client;
    }

    @Override
    public Quote getQuote(String ticker) {
        Quote cached = cache.get(ticker);
        if (cached != null && Duration.between(cached.getTimestamp(), Instant.now()).compareTo(TTL) < 0) {
            return cached;
        }

        String body;
        try {
            body = client.get("/quote", Map.of("symbol", ticker));
        } catch (FinnhubApiException exception) {
            throw new PriceFeedException(exception.getMessage(), exception);
        }

        // "c" is the current price. Finnhub answers an unknown symbol with a 200 and every
        // field zeroed rather than an error, so a zero here is a bad ticker, not a free stock.
        double price = new JSONObject(body).optDouble("c", 0.0);
        if (price <= 0.0) {
            throw new PriceFeedException("No quote for " + ticker + " — check the ticker symbol.");
        }

        Quote quote = new Quote(ticker, price, Instant.now());
        cache.put(ticker, quote);
        return quote;
    }
}
