package com.marketmaker.use_case.place_limit_stop_order;

import com.marketmaker.entities.Order;

public class PlaceLimitStopOrderRequestModel {
    private final String accountId;
    private final String ticker;
    private final Order.Side side;
    private final Order.Type type;
    private final int quantity;
    private final double triggerPrice;

    public PlaceLimitStopOrderRequestModel(String accountId, String ticker, Order.Side side,
                                           Order.Type type, int quantity, double triggerPrice) {
        this.accountId = accountId;
        this.ticker = ticker;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.triggerPrice = triggerPrice;
    }

    public String getAccountId() { return accountId; }
    public String getTicker() { return ticker; }
    public Order.Side getSide() { return side; }
    public Order.Type getType() { return type; }
    public int getQuantity() { return quantity; }
    public double getTriggerPrice() { return triggerPrice; }
}
