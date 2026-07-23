package com.marketmaker.use_case.view_candlestick_chart;

public class ViewCandlestickChartRequestModel {
    private final String ticker;
    private final Resolution resolution;

    public ViewCandlestickChartRequestModel(String ticker, Resolution resolution) {
        this.ticker = ticker;
        this.resolution = resolution;
    }

    public String getTicker() { return ticker; }
    public Resolution getResolution() { return resolution; }
}
