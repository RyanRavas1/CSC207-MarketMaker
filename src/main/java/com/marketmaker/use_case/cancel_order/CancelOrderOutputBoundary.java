package com.marketmaker.use_case.cancel_order;

// Implemented by the presenter/UI layer, not by the interactor.
public interface CancelOrderOutputBoundary {
    void presentSuccess(CancelOrderResponseModel response);
    void presentFailure(String errorMessage);
}
