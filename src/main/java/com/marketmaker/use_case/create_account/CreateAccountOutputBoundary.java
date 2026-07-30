package com.marketmaker.use_case.create_account;

public interface CreateAccountOutputBoundary {
    void presentSuccess(CreateAccountResponseModel response);
    void presentFailure(String errorMessage);
}
