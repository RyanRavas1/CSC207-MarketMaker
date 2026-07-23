package com.marketmaker.use_case.user_profile;

import java.util.List;

public class ViewProfileResponseModel {

    /** One holding with live valuation. */
    public static class Holding {
        private final String ticker;
        private final int shares;
        private final double averagePrice;
        private final double currentPrice;
        private final double marketValue;
        private final double unrealizedPnL;

        public Holding(String ticker, int shares, double averagePrice, double currentPrice,
                       double marketValue, double unrealizedPnL) {
            this.ticker = ticker;
            this.shares = shares;
            this.averagePrice = averagePrice;
            this.currentPrice = currentPrice;
            this.marketValue = marketValue;
            this.unrealizedPnL = unrealizedPnL;
        }

        public String getTicker() { return ticker; }
        public int getShares() { return shares; }
        public double getAveragePrice() { return averagePrice; }
        public double getCurrentPrice() { return currentPrice; }
        public double getMarketValue() { return marketValue; }
        public double getUnrealizedPnL() { return unrealizedPnL; }
    }

    private final String userName;
    private final double cashBalance;
    private final List<Holding> holdings;
    private final double totalEquity;

    public ViewProfileResponseModel(String userName, double cashBalance,
                                     List<Holding> holdings, double totalEquity) {
        this.userName = userName;
        this.cashBalance = cashBalance;
        this.holdings = holdings;
        this.totalEquity = totalEquity;
    }

    public String getUserName() { return userName; }
    public double getCashBalance() { return cashBalance; }
    public List<Holding> getHoldings() { return holdings; }
    public double getTotalEquity() { return totalEquity; }
}
