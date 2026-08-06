package com.marketmaker.interface_adapter.trading;

import com.marketmaker.use_case.cancel_order.CancelOrderOutputBoundary;
import com.marketmaker.use_case.cancel_order.CancelOrderResponseModel;

/** Drops the cancelled order's pending row from the trading screen. */
public class CancelOrderPresenter implements CancelOrderOutputBoundary {
    private final TradingViewModel viewModel;

    public CancelOrderPresenter(TradingViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess(CancelOrderResponseModel response) {
        TradingState state = new TradingState(viewModel.getState());
        state.removePendingOrder(response.getOrderId());
        state.setMessage("Order cancelled.");
        viewModel.publish(state);
    }

    @Override
    public void presentFailure(String errorMessage) {
        TradingState state = new TradingState(viewModel.getState());
        state.setMessage(errorMessage);
        viewModel.publish(state);
    }
}
