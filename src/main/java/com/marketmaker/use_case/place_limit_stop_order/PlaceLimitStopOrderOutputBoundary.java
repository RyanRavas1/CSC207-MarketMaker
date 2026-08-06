package com.marketmaker.use_case.place_limit_stop_order;

// Implemented by the presenter/UI layer, not by the interactor.
public interface PlaceLimitStopOrderOutputBoundary {
    void presentSuccess(PlaceLimitStopOrderResponseModel response);
    void presentFailure(String errorMessage);
}
