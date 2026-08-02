package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.view_order_history.ViewOrderHistoryOutputBoundary;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryResponseModel;

/** Publishes {@code ViewOrderHistoryInteractor} results to the order history panel. */
public class OrderHistoryPresenter implements ViewOrderHistoryOutputBoundary {

    private final ViewModel<ViewOrderHistoryResponseModel> viewModel;

    public OrderHistoryPresenter(ViewModel<ViewOrderHistoryResponseModel> viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess(ViewOrderHistoryResponseModel response) {
        viewModel.setState(response);
    }

    @Override
    public void presentFailure(String errorMessage) {
        viewModel.setError(errorMessage);
    }
}
