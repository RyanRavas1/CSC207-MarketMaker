package com.marketmaker.entities;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Account {

    private String userName;
    private double userBalance;
    private List<Position> holdings;
    private List<Order> placedOrders;
    private List<Trade> tradeLog;
    private Watchlist watchlist;
    // What the account was worth when it was first valued today. Day P/L is measured against
    // this mark, so it survives a restart the same way the rest of the account does.
    private double dayStartEquity;
    private LocalDate dayStartDate;

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

    /**
     * Swaps one holding for its updated self, or drops it when {@code updated} is null.
     * Position is immutable, so "changing" a holding means replacing the object — in place,
     * because remove-then-add would shuffle the row to the bottom of every portfolio table.
     */
    public void replacePosition(Position target, Position updated) {
        int index = this.holdings.indexOf(target);
        if (index < 0) {
            if (updated != null) {
                this.holdings.add(updated);
            }
        } else if (updated == null) {
            this.holdings.remove(index);
        } else {
            this.holdings.set(index, updated);
        }
    }

    /**
     * Cash already spoken for by buy orders that are still waiting to fill.
     *
     * <p>A resting buy is a promise to pay: leaving that cash on show as spendable lets the
     * user queue several orders they cannot collectively afford, and the surplus ones then sit
     * pending for ever because there is never enough money when their price arrives.
     *
     * @return the worst-case cost of every pending buy, valued at its trigger price
     */
    public double reservedForOpenBuys() {
        double reserved = 0.0;
        for (Order order : this.placedOrders) {
            if (order.getStatus() == Order.Status.PENDING
                    && order.getSide() == Order.Side.BUY
                    && order.getLimitOrStopPrice() != null) {
                reserved += order.getLimitOrStopPrice() * order.getQuantity();
            }
        }
        return reserved;
    }

    /** @return cash that is free to commit, once resting buys have taken their share */
    public double buyingPower() {
        return Math.max(0.0, this.userBalance - reservedForOpenBuys());
    }

    /**
     * Day P/L is what the account is worth now against what it was worth at the first
     * valuation of the day, so buying a stock doesn't move it (cash simply becomes shares)
     * but a price move or a realised gain does.
     */
    public double dailyPnL(LocalDate today, double totalEquity) {
        if (!today.equals(this.dayStartDate)) {
            this.dayStartDate = today;
            this.dayStartEquity = totalEquity;
        }
        return totalEquity - this.dayStartEquity;
    }

    // Cash is the whole of it: this is a cash account, with no margin and nothing held back
    // for resting buy orders, which are checked against the balance again when they fill.
    public double getBuyingPower() { return this.userBalance; }

    /** Sum of the realised gains and losses booked today. */
    public double realizedPnLOn(LocalDate day, ZoneId zone) {
        double total = 0.0;
        for (Trade trade : this.tradeLog) {
            if (trade.getRealizedPnL() != null && day.equals(trade.getTimestamp().atZone(zone).toLocalDate())) {
                total += trade.getRealizedPnL();
            }
        }
        return total;
    }

    public String getUserName() { return this.userName; }
    public double getUserBalance() { return this.userBalance; }
    public List<Position> getHoldings() { return this.holdings; }
    public List<Order> getPlacedOrders() { return this.placedOrders; }
    public List<Trade> getTradeLog() { return this.tradeLog; }
    public Watchlist getWatchlist() { return this.watchlist; }
    public double getDayStartEquity() { return this.dayStartEquity; }
    public LocalDate getDayStartDate() { return this.dayStartDate; }

    // Only the data access layer calls these, to restore a mark taken in an earlier session.
    public void setDayStartEquity(double dayStartEquity) { this.dayStartEquity = dayStartEquity; }
    public void setDayStartDate(LocalDate dayStartDate) { this.dayStartDate = dayStartDate; }
}
