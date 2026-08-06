package com.marketmaker.use_case.user_profile;

// Implemented by the presenter/UI layer, not by the interactor.
public interface ViewProfileOutputBoundary {
    void presentProfile(ViewProfileResponseModel response);
    void presentFailure(String errorMessage); // e.g. account not found
}
