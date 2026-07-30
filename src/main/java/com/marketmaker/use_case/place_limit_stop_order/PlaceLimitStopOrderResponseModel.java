package com.marketmaker.use_case.place_limit_stop_order;

import com.marketmaker.entities.Order;

public class PlaceLimitStopOrderResponseModel {
    private final String orderId;
    private final String ticker;
    private final Order.Type type;
    private final double triggerPrice;

    public PlaceLimitStopOrderResponseModel(String orderId, String ticker, Order.Type type, double triggerPrice) {
        this.orderId = orderId;
        this.ticker = ticker;
        this.type = type;
        this.triggerPrice = triggerPrice;
    }

    public String getOrderId() { return orderId; }
    public String getTicker() { return ticker; }
    public Order.Type getType() { return type; }
    public double getTriggerPrice() { return triggerPrice; }
}
