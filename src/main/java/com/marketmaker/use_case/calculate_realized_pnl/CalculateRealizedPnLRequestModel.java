package com.marketmaker.use_case.calculate_realized_pnl;

public class CalculateRealizedPnLRequestModel {
    private final String accountId;
    private final String ticker;
    private final int quantitySold;
    private final double salePrice;

    public CalculateRealizedPnLRequestModel(String accountId, String ticker, int quantitySold, double salePrice) {
        this.accountId = accountId;
        this.ticker = ticker;
        this.quantitySold = quantitySold;
        this.salePrice = salePrice;
    }

    public String getAccountId() { return accountId; }
    public String getTicker() { return ticker; }
    public int getQuantitySold() { return quantitySold; }
    public double getSalePrice() { return salePrice; }
}
