package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.view_positions.ViewPositionsOutputBoundary;
import com.marketmaker.use_case.view_positions.ViewPositionsResponseModel;

/** Publishes {@code ViewPositionsInteractor} results to the positions panel. */
public class PositionsPresenter implements ViewPositionsOutputBoundary {

    private final ViewModel<ViewPositionsResponseModel> viewModel;

    public PositionsPresenter(ViewModel<ViewPositionsResponseModel> viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess(ViewPositionsResponseModel response) {
        viewModel.setState(response);
    }

    @Override
    public void presentFailure(String errorMessage) {
        viewModel.setError(errorMessage);
    }
}
