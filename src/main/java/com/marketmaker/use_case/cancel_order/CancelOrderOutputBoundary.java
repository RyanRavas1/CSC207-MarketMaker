package com.marketmaker.use_case.cancel_order;

public interface CancelOrderOutputBoundary {
    void presentSuccess(CancelOrderResponseModel response);
    void presentFailure(String errorMessage);
}
