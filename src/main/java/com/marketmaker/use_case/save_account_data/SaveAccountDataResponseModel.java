package com.marketmaker.use_case.save_account_data;

public class SaveAccountDataResponseModel {
    private final String accountId;

    public SaveAccountDataResponseModel(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
