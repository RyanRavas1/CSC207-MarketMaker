package com.marketmaker.entities;

import java.util.ArrayList;
import java.util.List;

public class Account {

    private String userName;
    private double userBalance;
    private List<Position> holdings;
    private List<Order> placedOrders;
    private List<Trade> tradeLog;
    private Watchlist watchlist;

    public Account(String userName, double userBalance) {
        this.userName = userName;
        this.userBalance = userBalance;
        this.holdings = new ArrayList<>();
        this.placedOrders = new ArrayList<>();
        this.tradeLog = new ArrayList<>();
        this.watchlist = new Watchlist();
    }

    public double editBalance(double change) {
        this.userBalance += change;
        return this.userBalance;
    }

    public String changeUsername(String newUsername) {
        this.userName = newUsername;
        return this.userName;
    }

    // add new position to holdings
    public void addPosition(Position newPosition) {
        this.holdings.add(newPosition);
    }

    // add trade to tradeLog
    public void addTrade(Trade newTrade) {
        this.tradeLog.add(newTrade);
    }

    // add order to placedOrders
    public void addOrder(Order newOrder) {
        this.placedOrders.add(newOrder);
    }

    // remove order from placedOrders
    public void removeOrder(Order targetOrder) {
        this.placedOrders.remove(targetOrder);
    }

    // remove position from holdings
    public void removePosition(Position targetPosition) {
        this.holdings.remove(targetPosition);
    }

    public String getUserName() { return this.userName; }
    public double getUserBalance() { return this.userBalance; }
    public List<Position> getHoldings() { return this.holdings; }
    public List<Order> getPlacedOrders() { return this.placedOrders; }
    public List<Trade> getTradeLog() { return this.tradeLog; }
    public Watchlist getWatchlist() { return this.watchlist; }
}
