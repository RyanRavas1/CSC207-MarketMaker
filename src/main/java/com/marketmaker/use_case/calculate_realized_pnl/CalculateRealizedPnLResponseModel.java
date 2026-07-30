package com.marketmaker.use_case.calculate_realized_pnl;

public class CalculateRealizedPnLResponseModel {
    private final String ticker;
    private final int quantitySold;
    private final double realizedPnL;

    public CalculateRealizedPnLResponseModel(String ticker, int quantitySold, double realizedPnL) {
        this.ticker = ticker;
        this.quantitySold = quantitySold;
        this.realizedPnL = realizedPnL;
    }

    public String getTicker() { return ticker; }
    public int getQuantitySold() { return quantitySold; }
    public double getRealizedPnL() { return realizedPnL; }
}
