package com.marketmaker.entities;

public class Position {

    private final String ticker;
    private int shares; // must be non-negative
    private double averagePrice;
    public Position(String ticker, int shares, double averagePrice) {
        this.ticker = ticker;
        this.averagePrice = averagePrice;
        if (shares < 0) {
            this.shares = 0;
        }
    }
    public String getTicker() {
        return ticker;
    }
    public int getShares() {
        return shares;
    }
    public double getAveragePrice() {
        return averagePrice;
    }

}
