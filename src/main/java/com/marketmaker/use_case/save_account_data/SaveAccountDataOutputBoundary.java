package com.marketmaker.use_case.save_account_data;

public interface SaveAccountDataOutputBoundary {
    void presentSuccess(SaveAccountDataResponseModel response);
    void presentFailure(String errorMessage);
}
