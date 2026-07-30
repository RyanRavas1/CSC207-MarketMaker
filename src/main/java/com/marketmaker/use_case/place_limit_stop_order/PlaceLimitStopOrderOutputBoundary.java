package com.marketmaker.use_case.place_limit_stop_order;

public interface PlaceLimitStopOrderOutputBoundary {
    void presentSuccess(PlaceLimitStopOrderResponseModel response);
    void presentFailure(String errorMessage);
}
