package com.marketmaker.use_case.receive_live_quotes;

/**
 * Contract for a streaming market data source (e.g. a Finnhub WebSocket feed).
 * Owned by the use-case layer so callers never depend on the concrete transport.
 */
public interface LiveQuoteDataAccessInterface {
    void subscribe(String ticker, QuoteUpdateListener listener);

    void unsubscribe(String ticker);
}
