package com.marketmaker.interface_adapter.trading;

import java.time.Instant;

import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.match_pending_orders.MatchPendingOrdersInputBoundary;
import com.marketmaker.use_case.match_pending_orders.MatchPendingOrdersRequestModel;

/** Feeds an incoming price into the pending-order matcher. */
public class MatchPendingOrdersController {
    private final MatchPendingOrdersInputBoundary interactor;

    public MatchPendingOrdersController(MatchPendingOrdersInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void onQuote(String accountId, String ticker, double price) {
        interactor.execute(new MatchPendingOrdersRequestModel(
                accountId, new Quote(ticker, price, Instant.now())));
    }
}
