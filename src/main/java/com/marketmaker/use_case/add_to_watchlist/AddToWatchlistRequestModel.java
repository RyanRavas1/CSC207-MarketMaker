package com.marketmaker.use_case.add_to_watchlist;

public class AddToWatchlistRequestModel {
    private final String accountId;
    private final String ticker;

    public AddToWatchlistRequestModel(String accountId, String ticker) {
        this.accountId = accountId;
        this.ticker = ticker;
    }

    public String getAccountId() { return accountId; }
    public String getTicker() { return ticker; }
}
