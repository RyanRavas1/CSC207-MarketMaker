package com.marketmaker.entities;

import java.util.ArrayList;
import java.util.List;

public class Account {

    private String UserName;
    private double UserBalance;
    private List<Position> Holdings;
    private List<Order> PlacedOrder;
    private List<Trade> TradeLog;

    public Account (String UserName, double UserBalance){
        this.UserName = UserName;
        this.UserBalance = UserBalance;
        this.Holdings = new ArrayList<>();
        this.PlacedOrder = new ArrayList<>();
        this.TradeLog = new ArrayList<>();
    }

    public double edit_balance (double change){
        this.UserBalance += change;
        return this.UserBalance
    }

    public String change_username (String NewUsername){
        this.UserName = NewUsername;
        return this.UserName
    }

    //add new position to Holdings
    public void addPosition(Position NewPosition){
        this.Holdings.add(NewPosition);
    }

    //add Trade tI TradeLog
    public void addTrade(Trade NewTrade){
        this.TradeLog.add(NewTrade);
    }

    //add Order to PlacedOrder
    public void addOrder(Order NewOrder){
        this.PlacedOrder.add(NewOrder);
    }

    //remove Order to PlacedOrder
    public void removeOrder(Order TargetOrder){
        this.PlacedOrder.remove(TargetOrder);
    }

    //remove position to Holdings
    public void removePosition(Position TargetPosition){
        this.Holdings.remove(TargetPosition);
    }

    public String getUserName(){
        return this.UserName;
    }
    public double getUserBalance(){
        return this.UserBalance;
    }
    public List<Position> getHoldings(){
        return this.Holdings;
    }
    public List<Order> getPlacedOrder(){
        return this.PlacedOrder;
    }
    public List<Trade> getTradeLog(){
        return this.TradeLog;
    }

}
