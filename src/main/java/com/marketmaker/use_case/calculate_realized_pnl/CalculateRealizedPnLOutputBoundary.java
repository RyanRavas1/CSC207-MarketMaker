package com.marketmaker.use_case.calculate_realized_pnl;

public interface CalculateRealizedPnLOutputBoundary {
    void presentSuccess(CalculateRealizedPnLResponseModel response);
    void presentFailure(String errorMessage);
}
