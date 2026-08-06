package com.marketmaker.interface_adapter.trading;

import com.marketmaker.entities.Order;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderInputBoundary;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderRequestModel;

/** Converts the trading form into a limit or stop-loss order request. */
public class PlaceLimitStopOrderController {
    private final PlaceLimitStopOrderInputBoundary interactor;

    public PlaceLimitStopOrderController(PlaceLimitStopOrderInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void place(String accountId, String ticker, Order.Side side, Order.Type type,
                      int quantity, double triggerPrice) {
        interactor.execute(new PlaceLimitStopOrderRequestModel(
                accountId, ticker, side, type, quantity, triggerPrice));
    }
}
