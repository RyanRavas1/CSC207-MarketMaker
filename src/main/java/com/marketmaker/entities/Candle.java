package com.marketmaker.entities;

import java.time.LocalDateTime;

/** One OHLC bar for the chart. */
public class Candle {
    private final String ticker;
    private final String interval;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final double volume;
    private final LocalDateTime timestamp;

    public Candle(String ticker, String interval, double open, double high,
                  double low, double close, double volume, LocalDateTime timestamp) {
        this.ticker = ticker;
        this.interval = interval;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    public String getTicker() { return ticker; }
    public String getInterval() { return interval; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
    public double getVolume() { return volume; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
