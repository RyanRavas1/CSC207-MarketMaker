package com.marketmaker.use_case.remove_from_watchlist;

public class RemoveFromWatchlistRequestModel {
    private final String accountId;
    private final String ticker;

    public RemoveFromWatchlistRequestModel(String accountId, String ticker) {
        this.accountId = accountId;
        this.ticker = ticker;
    }

    public String getAccountId() { return accountId; }
    public String getTicker() { return ticker; }
}
