package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.view_trend_chart.ViewTrendChartOutputBoundary;
import com.marketmaker.use_case.view_trend_chart.ViewTrendChartResponseModel;

/** Publishes {@code ViewCandlestickChartInteractor} results to the chart panel. */
public class TrendChartPresenter implements ViewTrendChartOutputBoundary {

    private final ViewModel<ViewTrendChartResponseModel> viewModel;

    public TrendChartPresenter(ViewModel<ViewTrendChartResponseModel> viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess(ViewTrendChartResponseModel response) {
        viewModel.setState(response);
    }

    @Override
    public void presentFailure(String errorMessage) {
        viewModel.setError(errorMessage);
    }
}
