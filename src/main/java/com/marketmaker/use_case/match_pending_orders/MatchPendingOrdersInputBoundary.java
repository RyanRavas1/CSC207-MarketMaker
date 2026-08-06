package com.marketmaker.use_case.match_pending_orders;

/** Entry point called by controllers to run the match-pending-orders use case. */
public interface MatchPendingOrdersInputBoundary {
    void execute(MatchPendingOrdersRequestModel request);
}
