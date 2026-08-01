package com.marketmaker.use_case.view_positions;

public class ViewPositionsRequestModel {
    private final String accountId;

    public ViewPositionsRequestModel(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
