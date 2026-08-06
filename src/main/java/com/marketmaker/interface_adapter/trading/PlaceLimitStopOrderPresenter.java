package com.marketmaker.interface_adapter.trading;

import com.marketmaker.interface_adapter.Money;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderOutputBoundary;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderResponseModel;

/** Adds the accepted limit or stop order to the trading screen's pending rows. */
public class PlaceLimitStopOrderPresenter implements PlaceLimitStopOrderOutputBoundary {
    private final TradingViewModel viewModel;

    public PlaceLimitStopOrderPresenter(TradingViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentSuccess(PlaceLimitStopOrderResponseModel response) {
        TradingState state = new TradingState(viewModel.getState());
        state.getPendingOrders().add(new String[]{
                response.getOrderId(),
                response.getTicker(),
                response.getType().toString(),
                Money.format(response.getTriggerPrice())
        });
        state.setMessage(String.format("%s order on %s pending at %s.",
                response.getType(), response.getTicker(), Money.format(response.getTriggerPrice())));
        viewModel.publish(state);
    }

    @Override
    public void presentFailure(String errorMessage) {
        TradingState state = new TradingState(viewModel.getState());
        state.setMessage(errorMessage);
        viewModel.publish(state);
    }
}
