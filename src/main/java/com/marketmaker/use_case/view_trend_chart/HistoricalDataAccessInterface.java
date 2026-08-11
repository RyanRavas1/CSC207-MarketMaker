package com.marketmaker.use_case.view_trend_chart;

import java.util.List;

import com.marketmaker.entities.Candle;

/**
 * Contract for a historical OHLC data source (e.g. Finnhub's candles endpoint).
 * Owned by the use-case layer so it stays independent of the concrete provider.
 */
public interface HistoricalDataAccessInterface {
    // returns an empty list if no candles are available for the ticker/resolution
    List<Candle> fetchCandles(String ticker, Resolution resolution);
}
