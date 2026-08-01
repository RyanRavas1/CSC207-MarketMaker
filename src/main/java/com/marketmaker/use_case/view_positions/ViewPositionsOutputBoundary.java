package com.marketmaker.use_case.view_positions;

public interface ViewPositionsOutputBoundary {
    void presentSuccess(ViewPositionsResponseModel response);
    void presentFailure(String errorMessage);
}
