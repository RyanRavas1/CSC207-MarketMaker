package com.marketmaker.use_case.load_account_data;

public class LoadAccountDataResponseModel {
    private final String accountId;
    private final double userBalance;

    public LoadAccountDataResponseModel(String accountId, double userBalance) {
        this.accountId = accountId;
        this.userBalance = userBalance;
    }

    public String getAccountId() { return accountId; }
    public double getUserBalance() { return userBalance; }
}
