package com.marketmaker.use_case.save_account_data;

public class SaveAccountDataRequestModel {
    private final String accountId;

    public SaveAccountDataRequestModel(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
