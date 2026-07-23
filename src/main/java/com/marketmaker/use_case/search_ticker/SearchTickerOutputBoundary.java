package com.marketmaker.use_case.search_ticker;

public interface SearchTickerOutputBoundary {
    void presentSuccess(SearchTickerResponseModel response);
    void presentFailure(String errorMessage);
}
