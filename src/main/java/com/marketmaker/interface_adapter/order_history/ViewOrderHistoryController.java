package com.marketmaker.interface_adapter.order_history;

import com.marketmaker.use_case.order_history.ViewOrderHistoryInputBoundary;
import com.marketmaker.use_case.order_history.ViewOrderHistoryRequestModel;

/** Converts what the view has on screen into an order-history request. */
public class ViewOrderHistoryController {
    private final ViewOrderHistoryInputBoundary interactor;

    public ViewOrderHistoryController(ViewOrderHistoryInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void view(String accountId) {
        interactor.execute(new ViewOrderHistoryRequestModel(accountId));
    }
}
