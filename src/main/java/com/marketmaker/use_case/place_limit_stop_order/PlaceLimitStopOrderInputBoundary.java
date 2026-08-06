package com.marketmaker.use_case.place_limit_stop_order;

/** Entry point called by controllers to run the place-limit/stop-order use case. */
public interface PlaceLimitStopOrderInputBoundary {
    void execute(PlaceLimitStopOrderRequestModel request);
}
