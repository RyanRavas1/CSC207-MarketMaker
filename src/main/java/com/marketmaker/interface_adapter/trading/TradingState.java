package com.marketmaker.interface_adapter.trading;

import java.util.ArrayList;
import java.util.List;

/**
 * Display-ready trading screen: the four order use cases all write here because
 * they share one panel. A view model belongs to a view, not to a use case.
 */
public class TradingState {
    private String message = "";
    private String cashBalance = "";
    private List<String[]> pendingOrders = new ArrayList<>(); // {id, ticker, type, trigger}

    public TradingState() {
    }

    /** Carries the previous screen forward so one use case doesn't blank another's output. */
    public TradingState(TradingState previous) {
        this.message = previous.message;
        this.cashBalance = previous.cashBalance;
        this.pendingOrders = new ArrayList<>(previous.pendingOrders);
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCashBalance() { return cashBalance; }
    public void setCashBalance(String cashBalance) { this.cashBalance = cashBalance; }
    public List<String[]> getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(List<String[]> pendingOrders) { this.pendingOrders = pendingOrders; }

    /** Drops a pending row once its order is filled or cancelled. */
    public void removePendingOrder(String orderId) {
        pendingOrders.removeIf(row -> row[0].equals(orderId));
    }
}
