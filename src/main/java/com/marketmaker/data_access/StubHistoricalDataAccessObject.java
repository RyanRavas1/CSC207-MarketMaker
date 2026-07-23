package com.marketmaker.data_access;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.marketmaker.entities.Candle;
import com.marketmaker.use_case.view_candlestick_chart.HistoricalDataAccessInterface;
import com.marketmaker.use_case.view_candlestick_chart.Resolution;

/**
 * Placeholder historical data source until a real provider (e.g. Finnhub) is
 * wired in. Generates a synthetic random-walk series of candles.
 */
public class StubHistoricalDataAccessObject implements HistoricalDataAccessInterface {
    private static final Set<String> KNOWN_TICKERS = Set.of("AAPL", "MSFT", "NVDA", "TSLA");
    private static final int CANDLE_COUNT = 30;

    @Override
    public List<Candle> fetchCandles(String ticker, Resolution resolution) {
        if (!KNOWN_TICKERS.contains(ticker)) {
            return List.of();
        }

        Duration step = stepFor(resolution);
        Instant now = Instant.now();
        double price = 100.0;

        List<Candle> candles = new ArrayList<>();
        for (int i = CANDLE_COUNT - 1; i >= 0; i--) {
            double open = price;
            double close = open + (Math.random() - 0.5) * 4;
            double high = Math.max(open, close) + Math.random() * 2;
            double low = Math.min(open, close) - Math.random() * 2;
            double volume = 1000 + Math.random() * 500;
            Instant timestamp = now.minus(step.multipliedBy(i));

            candles.add(new Candle(ticker, resolutionLabel(resolution), open, high, low, close, volume, timestamp));
            price = close;
        }

        return candles;
    }

    private Duration stepFor(Resolution resolution) {
        return switch (resolution) {
            case ONE_MINUTE -> Duration.ofMinutes(1);
            case FIVE_MINUTE -> Duration.ofMinutes(5);
            case ONE_DAY -> Duration.ofDays(1);
        };
    }

    private String resolutionLabel(Resolution resolution) {
        return switch (resolution) {
            case ONE_MINUTE -> "1";
            case FIVE_MINUTE -> "5";
            case ONE_DAY -> "D";
        };
    }
}
