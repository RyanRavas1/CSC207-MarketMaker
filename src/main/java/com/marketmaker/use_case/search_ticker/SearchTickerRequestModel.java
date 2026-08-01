package com.marketmaker.use_case.search_ticker;

public class SearchTickerRequestModel {
    private final String ticker;

    public SearchTickerRequestModel(String ticker) {
        this.ticker = ticker;
    }

    public String getTicker() { return ticker; }
}
