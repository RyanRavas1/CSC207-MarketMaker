package com.marketmaker.use_case.view_trend_chart;

public interface ViewTrendChartInputBoundary {
    // Also used for Switch Chart Interval: call again with a different resolution to reload.
    void execute(ViewTrendChartRequestModel request);
}
