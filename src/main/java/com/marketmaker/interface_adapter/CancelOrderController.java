package com.marketmaker.interface_adapter;

import java.util.concurrent.Executor;

import com.marketmaker.use_case.cancel_order.CancelOrderInputBoundary;
import com.marketmaker.use_case.cancel_order.CancelOrderRequestModel;

/**
 * Turns the order the user picked out of the history into a cancel request.
 *
 * <p>Holds the account id the same way the order ticket does, so the view passes only what
 * the user actually chose.
 *
 * <p>Dispatched on the worker, like every other use case that writes the account. See
 * {@link WatchlistController} for why: these interactors read the account, change it and
 * write it back, and two of them doing that on different threads loses whichever save
 * landed first.
 */
public class CancelOrderController {
    private final CancelOrderInputBoundary interactor;
    private final Executor worker;
    private final String accountId;

    public CancelOrderController(CancelOrderInputBoundary interactor, Executor worker, String accountId) {
        this.interactor = interactor;
        this.worker = worker;
        this.accountId = accountId;
    }

    public void cancel(String orderId) {
        worker.execute(() -> interactor.execute(new CancelOrderRequestModel(accountId, orderId)));
    }
}
