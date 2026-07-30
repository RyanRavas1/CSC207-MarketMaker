package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryOutputBoundary;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;

/** Publishes {@code ViewPortfolioSummaryInteractor} results to the account summary bar. */
public class PortfolioSummaryPresenter implements ViewPortfolioSummaryOutputBoundary {

    private final ViewModel<ViewPortfolioSummaryResponseModel> viewModel;

    public PortfolioSummaryPresenter(ViewModel<ViewPortfolioSummaryResponseModel> viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess(ViewPortfolioSummaryResponseModel response) {
        viewModel.setState(response);
    }

    @Override
    public void presentFailure(String errorMessage) {
        viewModel.setError(errorMessage);
    }
}
