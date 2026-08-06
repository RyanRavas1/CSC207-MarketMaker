package com.marketmaker.interface_adapter.trading;

import com.marketmaker.use_case.cancel_order.CancelOrderInputBoundary;
import com.marketmaker.use_case.cancel_order.CancelOrderRequestModel;

/** Converts a selected pending row into a cancel request. */
public class CancelOrderController {
    private final CancelOrderInputBoundary interactor;

    public CancelOrderController(CancelOrderInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void cancel(String accountId, String orderId) {
        interactor.execute(new CancelOrderRequestModel(accountId, orderId));
    }
}
