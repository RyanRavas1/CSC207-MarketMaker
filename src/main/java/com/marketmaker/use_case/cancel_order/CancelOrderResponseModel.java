package com.marketmaker.use_case.cancel_order;

public class CancelOrderResponseModel {
    private final String orderId;

    public CancelOrderResponseModel(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() { return orderId; }
}
