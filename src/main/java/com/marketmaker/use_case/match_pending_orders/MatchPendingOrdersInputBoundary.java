package com.marketmaker.use_case.match_pending_orders;

public interface MatchPendingOrdersInputBoundary {
    void execute(MatchPendingOrdersRequestModel request);
}
