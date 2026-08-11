package com.marketmaker.use_case.view_trend_chart;

public interface ViewTrendChartOutputBoundary {
    void presentSuccess(ViewTrendChartResponseModel response);
    void presentFailure(String errorMessage);
}
