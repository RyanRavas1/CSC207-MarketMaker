package com.marketmaker.use_case.cancel_order;

/** Entry point called by controllers to run the cancel-order use case. */
public interface CancelOrderInputBoundary {
    void execute(CancelOrderRequestModel request);
}
