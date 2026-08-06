package com.marketmaker.interface_adapter.trading;

import com.marketmaker.entities.Order;
import com.marketmaker.use_case.place_order.PlaceOrderInputBoundary;
import com.marketmaker.use_case.place_order.PlaceOrderRequestModel;

/** Converts the trading form into a market-order request. */
public class PlaceOrderController {
    private final PlaceOrderInputBoundary interactor;

    public PlaceOrderController(PlaceOrderInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void place(String accountId, String ticker, Order.Side side, int quantity) {
        interactor.execute(new PlaceOrderRequestModel(accountId, ticker, side, quantity));
    }
}
