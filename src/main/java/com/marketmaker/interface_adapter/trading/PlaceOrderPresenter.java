package com.marketmaker.interface_adapter.trading;

import com.marketmaker.interface_adapter.Money;
import com.marketmaker.use_case.place_order.PlaceOrderOutputBoundary;
import com.marketmaker.use_case.place_order.PlaceOrderResponseModel;

/** Turns a market-order fill into a line the trading screen can render as-is. */
public class PlaceOrderPresenter implements PlaceOrderOutputBoundary {
    private final TradingViewModel viewModel;

    public PlaceOrderPresenter(TradingViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess(PlaceOrderResponseModel response) {
        TradingState state = new TradingState(viewModel.getState());
        state.setMessage(String.format("Filled %d %s @ %s — you now hold %d shares.",
                response.getQuantity(), response.getTicker(), Money.format(response.getFillPrice()),
                response.getNewShareCount()));
        state.setCashBalance(Money.format(response.getNewCashBalance()));
        viewModel.publish(state);
    }

    @Override
    public void presentFailure(String errorMessage) {
        TradingState state = new TradingState(viewModel.getState());
        state.setMessage(errorMessage);
        viewModel.publish(state);
    }
}
