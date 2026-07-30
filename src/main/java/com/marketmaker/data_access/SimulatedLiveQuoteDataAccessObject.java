package com.marketmaker.data_access;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.receive_live_quotes.LiveQuoteDataAccessInterface;
import com.marketmaker.use_case.receive_live_quotes.QuoteUpdateListener;

/**
 * Stands in for a real Finnhub WebSocket feed: ticks a random-walk price for
 * each subscribed ticker on a fixed interval until unsubscribed.
 */
public class SimulatedLiveQuoteDataAccessObject implements LiveQuoteDataAccessInterface {
    private static final long TICK_INTERVAL_MS = 1000;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, Double> lastPrice = new ConcurrentHashMap<>();

    @Override
    public void subscribe(String ticker, QuoteUpdateListener listener) {
        unsubscribe(ticker);
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            double base = lastPrice.getOrDefault(ticker, 100.0);
            double next = base + (Math.random() - 0.5) * 2;
            lastPrice.put(ticker, next);
            listener.onQuote(new Quote(ticker, next, Instant.now()));
        }, 0, TICK_INTERVAL_MS, TimeUnit.MILLISECONDS);
        subscriptions.put(ticker, task);
    }

    @Override
    public void unsubscribe(String ticker) {
        ScheduledFuture<?> task = subscriptions.remove(ticker);
        if (task != null) {
            task.cancel(false);
        }
    }
}
