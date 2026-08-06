package com.marketmaker.use_case.order_history;

/** Entry point called by controllers to run the order-history use case. */
public interface ViewOrderHistoryInputBoundary {
    void execute(ViewOrderHistoryRequestModel request);
}
