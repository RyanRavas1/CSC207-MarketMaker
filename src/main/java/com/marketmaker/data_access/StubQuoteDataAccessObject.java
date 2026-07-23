package com.marketmaker.data_access;

import java.time.Instant;
import java.util.Map;

import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.search_ticker.TickerDataAccessInterface;

/**
 * Placeholder market data source until a real provider (e.g. Finnhub) is
 * wired in. Serves a small fixed set of tickers with a randomized last price.
 */
public class StubQuoteDataAccessObject implements TickerDataAccessInterface {
    private static final Map<String, Double> KNOWN_TICKERS = Map.of(
            "AAPL", 232.50,
            "MSFT", 425.10,
            "NVDA", 118.00,
            "TSLA", 240.00
    );

    @Override
    public Quote fetchQuote(String ticker) {
        Double basePrice = KNOWN_TICKERS.get(ticker);
        if (basePrice == null) {
            return null;
        }
        double price = basePrice + (Math.random() - 0.5) * 2;
        return new Quote(ticker, price, Instant.now());
    }
}
