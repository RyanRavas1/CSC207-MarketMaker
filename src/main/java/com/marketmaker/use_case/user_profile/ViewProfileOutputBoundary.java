package com.marketmaker.use_case.user_profile;

public interface ViewProfileOutputBoundary {
    void presentProfile(ViewProfileResponseModel response);
    void presentFailure(String errorMessage);
}
