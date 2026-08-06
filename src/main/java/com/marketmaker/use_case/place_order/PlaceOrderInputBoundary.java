package com.marketmaker.use_case.place_order;

/** Entry point called by controllers to run the place-market-order use case. */
public interface PlaceOrderInputBoundary {
    void execute(PlaceOrderRequestModel request);
}
