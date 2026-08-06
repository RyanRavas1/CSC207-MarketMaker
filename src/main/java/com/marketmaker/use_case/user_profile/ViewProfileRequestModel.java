package com.marketmaker.use_case.user_profile;

public class ViewProfileRequestModel {
    private final String accountId; // currently the account's username, see AccountDAO

    public ViewProfileRequestModel(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
