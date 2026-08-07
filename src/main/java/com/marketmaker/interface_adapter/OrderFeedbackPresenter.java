package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderOutputBoundary;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderResponseModel;
import com.marketmaker.use_case.place_order.PlaceOrderOutputBoundary;
import com.marketmaker.use_case.place_order.PlaceOrderResponseModel;
import com.marketmaker.view.Format;

/**
 * Says what happened to an order, in one line under the ticket.
 *
 * <p>Serves both order use cases because the ticket is one form: whichever way an order was
 * routed, the user is looking at the same place for the answer. Every outcome also reloads
 * the screens that a fill changes — cash, holdings and the log.
 */
public class OrderFeedbackPresenter implements PlaceOrderOutputBoundary, PlaceLimitStopOrderOutputBoundary {
    private final ViewModel<String> status;
    private final Runnable refresh;

    public OrderFeedbackPresenter(ViewModel<String> status, Runnable refresh) {
        this.status = status;
        this.refresh = refresh;
    }

    @Override
    public void presentSuccess(PlaceOrderResponseModel response) {
        status.setState(String.format("Filled %d %s at $%s — you now hold %d shares.",
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
    public void presentFailure(String errorMessage) {
        status.setState(errorMessage);
        refresh.run();
    }
}
