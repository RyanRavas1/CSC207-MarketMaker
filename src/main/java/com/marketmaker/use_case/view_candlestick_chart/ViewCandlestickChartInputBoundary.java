package com.marketmaker.use_case.view_candlestick_chart;

public interface ViewCandlestickChartInputBoundary {
    // Also used for Switch Chart Interval: call again with a different resolution to reload.
    void execute(ViewCandlestickChartRequestModel request);
}
