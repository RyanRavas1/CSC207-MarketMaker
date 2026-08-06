package com.marketmaker.interface_adapter.user_profile;

import java.util.ArrayList;
import java.util.List;

/** Display-ready profile: everything is already formatted for the view. */
public class ViewProfileState {
    private String userName = "";
    private String cashBalance = "";
    private String totalEquity = "";
    private String buyingPower = "";
    private String realizedPnLToday = "";
    private String dailyPnL = "";
    private List<String[]> holdings = new ArrayList<>(); // {ticker, shares, avg, last, value, P/L}
    private String error = "";

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getCashBalance() { return cashBalance; }
    public void setCashBalance(String cashBalance) { this.cashBalance = cashBalance; }
    public String getTotalEquity() { return totalEquity; }
    public void setTotalEquity(String totalEquity) { this.totalEquity = totalEquity; }
    public String getBuyingPower() { return buyingPower; }
    public void setBuyingPower(String buyingPower) { this.buyingPower = buyingPower; }
    public String getRealizedPnLToday() { return realizedPnLToday; }
    public void setRealizedPnLToday(String realizedPnLToday) { this.realizedPnLToday = realizedPnLToday; }
    public String getDailyPnL() { return dailyPnL; }
    public void setDailyPnL(String dailyPnL) { this.dailyPnL = dailyPnL; }
    public List<String[]> getHoldings() { return holdings; }
    public void setHoldings(List<String[]> holdings) { this.holdings = holdings; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
