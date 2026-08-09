package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.cancel_order.CancelOrderInputBoundary;
import com.marketmaker.use_case.cancel_order.CancelOrderRequestModel;

/**
 * Turns the order the user picked out of the history into a cancel request.
 *
 * <p>Holds the account id the same way the order ticket does, so the view passes only what
 * the user actually chose.
 */
public class CancelOrderController {
    private final CancelOrderInputBoundary interactor;
    private final String accountId;

    public CancelOrderController(CancelOrderInputBoundary interactor, String accountId) {
        this.interactor = interactor;
        this.accountId = accountId;
    }

    public void cancel(String orderId) {
        interactor.execute(new CancelOrderRequestModel(accountId, orderId));
    }
}
