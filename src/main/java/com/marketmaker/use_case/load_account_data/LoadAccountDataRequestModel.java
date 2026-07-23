package com.marketmaker.use_case.load_account_data;

public class LoadAccountDataRequestModel {
    private final String accountId;

    public LoadAccountDataRequestModel(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
