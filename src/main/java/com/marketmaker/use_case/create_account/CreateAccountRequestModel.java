package com.marketmaker.use_case.create_account;

public class CreateAccountRequestModel {
    private final String accountId;

    public CreateAccountRequestModel(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
