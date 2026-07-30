package com.marketmaker.use_case.cancel_order;

public class CancelOrderRequestModel {
    private final String accountId;
    private final String orderId;

    public CancelOrderRequestModel(String accountId, String orderId) {
        this.accountId = accountId;
        this.orderId = orderId;
    }

    public String getAccountId() { return accountId; }
    public String getOrderId() { return orderId; }
}
