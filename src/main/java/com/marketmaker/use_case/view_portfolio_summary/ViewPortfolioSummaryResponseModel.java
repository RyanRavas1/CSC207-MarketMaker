package com.marketmaker.use_case.view_portfolio_summary;

public class ViewPortfolioSummaryResponseModel {
    private final double cash;
    private final double buyingPower;
    private final double totalEquity;
    private final double dailyPnL;

    public ViewPortfolioSummaryResponseModel(double cash, double buyingPower,
                                              double totalEquity, double dailyPnL) {
        this.cash = cash;
        this.buyingPower = buyingPower;
        this.totalEquity = totalEquity;
        this.dailyPnL = dailyPnL;
    }

    public double getCash() { return cash; }
    public double getBuyingPower() { return buyingPower; }
    public double getTotalEquity() { return totalEquity; }
    public double getDailyPnL() { return dailyPnL; }
}
