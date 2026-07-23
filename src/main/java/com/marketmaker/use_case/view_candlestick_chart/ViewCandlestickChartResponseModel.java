package com.marketmaker.use_case.view_candlestick_chart;

import java.util.List;

import com.marketmaker.entities.Candle;

public class ViewCandlestickChartResponseModel {
    private final String ticker;
    private final Resolution resolution;
    private final List<Candle> candles;

    public ViewCandlestickChartResponseModel(String ticker, Resolution resolution, List<Candle> candles) {
        this.ticker = ticker;
        this.resolution = resolution;
        this.candles = candles;
    }

    public String getTicker() { return ticker; }
    public Resolution getResolution() { return resolution; }
    public List<Candle> getCandles() { return candles; }
}
