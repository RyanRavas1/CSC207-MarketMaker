package com.marketmaker.use_case.view_order_history;

public interface ViewOrderHistoryOutputBoundary {
    void presentSuccess(ViewOrderHistoryResponseModel response);
    void presentFailure(String errorMessage);
}
