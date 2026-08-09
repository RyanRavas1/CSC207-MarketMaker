package com.marketmaker.use_case.place_order;

public interface PlaceOrderOutputBoundary {
    void presentSuccess(PlaceOrderResponseModel response);
    void presentFailure(String errorMessage);
}
