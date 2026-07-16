package com.marketmaker.use_case.match_pending_orders;

public class MatchPendingOrdersResponseModel {
    private final String orderId;
    private final String ticker;
    private final double fillPrice;
    private final double newCashBalance;
    private final int newShareCount;

    public MatchPendingOrdersResponseModel(String orderId, String ticker, double fillPrice,
                                            double newCashBalance, int newShareCount) {
        this.orderId = orderId;
        this.ticker = ticker;
        this.fillPrice = fillPrice;
        this.newCashBalance = newCashBalance;
        this.newShareCount = newShareCount;
    }

    public String getOrderId() { return orderId; }
    public String getTicker() { return ticker; }
    public double getFillPrice() { return fillPrice; }
    public double getNewCashBalance() { return newCashBalance; }
    public int getNewShareCount() { return newShareCount; }
}
