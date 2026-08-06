package com.marketmaker.use_case.order_history;

// Implemented by the presenter/UI layer, not by the interactor.
public interface ViewOrderHistoryOutputBoundary {
    void presentHistory(ViewOrderHistoryResponseModel response);
    void presentFailure(String errorMessage); // e.g. account not found
}
