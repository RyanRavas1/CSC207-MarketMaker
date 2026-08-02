package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartOutputBoundary;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartResponseModel;

/** Publishes {@code ViewCandlestickChartInteractor} results to the chart panel. */
public class CandlestickChartPresenter implements ViewCandlestickChartOutputBoundary {

    private final ViewModel<ViewCandlestickChartResponseModel> viewModel;

    public CandlestickChartPresenter(ViewModel<ViewCandlestickChartResponseModel> viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess(ViewCandlestickChartResponseModel response) {
        viewModel.setState(response);
    }

    @Override
    public void presentFailure(String errorMessage) {
        viewModel.setError(errorMessage);
    }
}
