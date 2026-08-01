package com.marketmaker.use_case.view_portfolio_summary;

public class ViewPortfolioSummaryRequestModel {
    private final String accountId;

    public ViewPortfolioSummaryRequestModel(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
