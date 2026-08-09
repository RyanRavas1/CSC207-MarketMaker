package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.user_profile.ViewProfileOutputBoundary;
import com.marketmaker.use_case.user_profile.ViewProfileResponseModel;

/** Publishes the account overview to whichever screen is showing it. */
public class ProfilePresenter implements ViewProfileOutputBoundary {
    private final ViewModel<ViewProfileResponseModel> viewModel;

    public ProfilePresenter(ViewModel<ViewProfileResponseModel> viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentProfile(ViewProfileResponseModel response) {
        viewModel.setState(response);
    }

    @Override
    public void presentFailure(String errorMessage) {
        viewModel.setError(errorMessage);
    }
}
