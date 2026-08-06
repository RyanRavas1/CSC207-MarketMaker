package com.marketmaker.use_case.place_order;

// Implemented by the presenter/UI layer, not by the interactor.
public interface PlaceOrderOutputBoundary {
    void presentSuccess(PlaceOrderResponseModel response);
    void presentFailure(String errorMessage);
}
