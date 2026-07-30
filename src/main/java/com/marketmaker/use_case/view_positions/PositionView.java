package com.marketmaker.use_case.view_positions;

/** One row of the positions table: a holding priced at the current market quote. */
public class PositionView {
    private final String ticker;
    private final int shares;
    private final double averageCost;
    private final double currentPrice;
    private final double unrealizedPnL;

    public PositionView(String ticker, int shares, double averageCost,
                         double currentPrice, double unrealizedPnL) {
        this.ticker = ticker;
        this.shares = shares;
        this.averageCost = averageCost;
        this.currentPrice = currentPrice;
        this.unrealizedPnL = unrealizedPnL;
    }

    public String getTicker() { return ticker; }
    public int getShares() { return shares; }
    public double getAverageCost() { return averageCost; }
    public double getCurrentPrice() { return currentPrice; }
    public double getUnrealizedPnL() { return unrealizedPnL; }
}
