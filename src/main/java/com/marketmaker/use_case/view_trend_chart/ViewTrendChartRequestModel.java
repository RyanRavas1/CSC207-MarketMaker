package com.marketmaker.use_case.view_trend_chart;

public class ViewTrendChartRequestModel {
    private final String ticker;
    private final Resolution resolution;

    public ViewTrendChartRequestModel(String ticker, Resolution resolution) {
        this.ticker = ticker;
        this.resolution = resolution;
    }

    public String getTicker() { return ticker; }
    public Resolution getResolution() { return resolution; }
}
