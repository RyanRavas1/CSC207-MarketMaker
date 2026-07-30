package com.marketmaker.use_case.create_account;

public class CreateAccountResponseModel {
    private final String accountId;
    private final double startingBalance;

    public CreateAccountResponseModel(String accountId, double startingBalance) {
        this.accountId = accountId;
        this.startingBalance = startingBalance;
    }

    public String getAccountId() { return accountId; }
    public double getStartingBalance() { return startingBalance; }
}
