package com.marketmaker.use_case.view_candlestick_chart;

public interface ViewCandlestickChartOutputBoundary {
    void presentSuccess(ViewCandlestickChartResponseModel response);
    void presentFailure(String errorMessage);
}
