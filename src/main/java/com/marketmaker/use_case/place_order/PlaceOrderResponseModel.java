package com.marketmaker.use_case.place_order;

public class PlaceOrderResponseModel {
    private final String ticker;
    private final int quantity;
    private final double fillPrice;
    private final double newCashBalance;
    private final int newShareCount;

    public PlaceOrderResponseModel(String ticker, int quantity, double fillPrice,
                                   double newCashBalance, int newShareCount) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.fillPrice = fillPrice;
        this.newCashBalance = newCashBalance;
        this.newShareCount = newShareCount;
    }

    public String getTicker() { return ticker; }
    public int getQuantity() { return quantity; }
    public double getFillPrice() { return fillPrice; }
    public double getNewCashBalance() { return newCashBalance; }
    public int getNewShareCount() { return newShareCount; }
}
