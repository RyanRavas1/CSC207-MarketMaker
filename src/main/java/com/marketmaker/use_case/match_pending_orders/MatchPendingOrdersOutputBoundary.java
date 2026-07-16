package com.marketmaker.use_case.match_pending_orders;

// Called for each order filled by an incoming quote (0..N times per execute()).
// If the quote fills no orders, this callback isn't called.
public interface MatchPendingOrdersOutputBoundary {
    void presentFill(MatchPendingOrdersResponseModel response);
}
