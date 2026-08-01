package com.marketmaker.use_case.view_portfolio_summary;

public interface ViewPortfolioSummaryOutputBoundary {
    void presentSuccess(ViewPortfolioSummaryResponseModel response);
    void presentFailure(String errorMessage);
}
