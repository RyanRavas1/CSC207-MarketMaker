package com.marketmaker.use_case.place_order;

import com.marketmaker.entities.Order;

public class PlaceOrderRequestModel {
    private final String accountId;
    private final String ticker;
    private final Order.Side side;
    private final int quantity;

    public PlaceOrderRequestModel(String accountId, String ticker, Order.Side side, int quantity) {
        this.accountId = accountId;
        this.ticker = ticker;
        this.side = side;
        this.quantity = quantity;
    }

    public String getAccountId() { return accountId; }
    public String getTicker() { return ticker; }
    public Order.Side getSide() { return side; }
    public int getQuantity() { return quantity; }
}
