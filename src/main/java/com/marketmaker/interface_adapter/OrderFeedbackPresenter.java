package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.cancel_order.CancelOrderOutputBoundary;
import com.marketmaker.use_case.cancel_order.CancelOrderResponseModel;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderOutputBoundary;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderResponseModel;
import com.marketmaker.use_case.place_order.PlaceOrderOutputBoundary;
import com.marketmaker.use_case.place_order.PlaceOrderResponseModel;

/**
 * Says what happened to an order, in one line under the ticket.
 *
 * <p>Serves every order use case because an order's outcome reads the same wherever it came
 * from: placed, rested or cancelled. Each outcome also reloads the screens that it changes -
 * cash, holdings and the log.
 */
public class OrderFeedbackPresenter implements PlaceOrderOutputBoundary,
        PlaceLimitStopOrderOutputBoundary, CancelOrderOutputBoundary {
    private final ViewModel<String> status;
    private final Runnable refresh;

    public OrderFeedbackPresenter(ViewModel<String> status, Runnable refresh) {
        this.status = status;
        this.refresh = refresh;
    }

    @Override
    public void presentSuccess(PlaceOrderResponseModel response) {
        status.setState(String.format("Filled %d %s at $%s - you now hold %d shares.",
                response.getQuantity(), response.getTicker(),
                Format.money(response.getFillPrice()), response.getNewShareCount()));
        refresh.run();
    }

    @Override
    public void presentSuccess(PlaceLimitStopOrderResponseModel response) {
        status.setState(String.format("%s order on %s resting at $%s.",
                response.getType(), response.getTicker(), Format.money(response.getTriggerPrice())));
        refresh.run();
    }

    @Override
    public void presentSuccess(CancelOrderResponseModel response) {
        status.setState("Order cancelled.");
        refresh.run();
    }

    @Override
    public void presentFailure(String errorMessage) {
        // The error channel, not the state channel: a rejection reads differently from a fill,
        // and views that show both need to tell them apart to colour them.
        status.setError(errorMessage);
        refresh.run();
    }
}
