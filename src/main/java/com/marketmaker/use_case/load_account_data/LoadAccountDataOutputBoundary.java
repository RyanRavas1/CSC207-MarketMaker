package com.marketmaker.use_case.load_account_data;

public interface LoadAccountDataOutputBoundary {
    void presentSuccess(LoadAccountDataResponseModel response);
    void presentFailure(String errorMessage);
}
