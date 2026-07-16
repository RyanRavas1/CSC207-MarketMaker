package com.marketmaker.use_case.match_pending_orders;

import com.marketmaker.entities.Quote;

public class MatchPendingOrdersRequestModel {
    private final String accountId;
    private final Quote quote;

    public MatchPendingOrdersRequestModel(String accountId, Quote quote) {
        this.accountId = accountId;
        this.quote = quote;
    }

    public String getAccountId() { return accountId; }
    public Quote getQuote() { return quote; }
}
