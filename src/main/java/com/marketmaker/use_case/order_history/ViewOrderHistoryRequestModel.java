package com.marketmaker.use_case.order_history;

public class ViewOrderHistoryRequestModel {
    private final String accountId; // currently the account's username, see AccountDAO

    public ViewOrderHistoryRequestModel(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
