package com.marketmaker.interface_adapter.trading;

import com.marketmaker.interface_adapter.Money;
import com.marketmaker.use_case.match_pending_orders.MatchPendingOrdersOutputBoundary;
import com.marketmaker.use_case.match_pending_orders.MatchPendingOrdersResponseModel;

/** Called once per order the incoming quote filled; no failure path on this boundary. */
public class MatchPendingOrdersPresenter implements MatchPendingOrdersOutputBoundary {
    private final TradingViewModel viewModel;

    public MatchPendingOrdersPresenter(TradingViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentFill(MatchPendingOrdersResponseModel response) {
        TradingState state = new TradingState(viewModel.getState());
        state.removePendingOrder(response.getOrderId());
        state.setMessage(String.format("Pending %s order filled @ %s — you now hold %d shares.",
                response.getTicker(), Money.format(response.getFillPrice()), response.getNewShareCount()));
        state.setCashBalance(Money.format(response.getNewCashBalance()));
        viewModel.publish(state);
    }
}
