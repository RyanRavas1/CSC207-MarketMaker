package com.marketmaker.use_case.search_ticker;

import com.marketmaker.entities.Quote;

/**
 * Contract for anything that can look up a live quote for a ticker.
 * Owned by the use-case layer so it stays independent of whichever
 * concrete market data provider (e.g. Finnhub) backs it.
 */
public interface TickerDataAccessInterface {
    // returns null if the ticker is unknown to the data provider
    Quote fetchQuote(String ticker);
}
