package com.marketmaker.use_case.view_order_history;

public class ViewOrderHistoryRequestModel {
    private final String accountId;

    public ViewOrderHistoryRequestModel(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
