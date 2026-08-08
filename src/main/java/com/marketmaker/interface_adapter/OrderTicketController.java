package com.marketmaker.interface_adapter;

import com.marketmaker.entities.Order;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderInputBoundary;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderRequestModel;
import com.marketmaker.use_case.place_order.PlaceOrderInputBoundary;
import com.marketmaker.use_case.place_order.PlaceOrderRequestModel;

/**
 * Turns the order ticket into a request and routes it to the use case that handles that
 * order type: market orders fill immediately, limit and stop orders rest until the price
 * reaches them.
 */
public class OrderTicketController {
    private final PlaceOrderInputBoundary marketInteractor;
    private final PlaceLimitStopOrderInputBoundary restingInteractor;
    private final String accountId;

    public OrderTicketController(PlaceOrderInputBoundary marketInteractor,
                                 PlaceLimitStopOrderInputBoundary restingInteractor,
                                 String accountId) {
        this.marketInteractor = marketInteractor;
        this.restingInteractor = restingInteractor;
        this.accountId = accountId;
    }

    /**
     * @param trigger the limit or stop price, ignored for a market order
     * @return null when the ticket was sent, or the reason it could not be read
     */
    public String place(String ticker, Order.Side side, Order.Type type,
                        String quantityText, String triggerText) {
        String symbol = ticker == null ? "" : ticker.trim().toUpperCase();
        if (symbol.isEmpty()) {
            return "Enter a ticker.";
        }

        // The form is free text, so bad numbers are caught here rather than reaching a use
        // case that can only report them as an order failure.
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText.trim());
        } catch (NumberFormatException exception) {
            return "Quantity must be a whole number.";
        }

        if (type == Order.Type.MARKET) {
            marketInteractor.execute(new PlaceOrderRequestModel(accountId, symbol, side, quantity));
            return null;
        }

        double trigger;
        try {
            trigger = Double.parseDouble(triggerText.trim());
        } catch (NumberFormatException exception) {
            return type == Order.Type.LIMIT ? "Enter a limit price." : "Enter a stop price.";
        }

        restingInteractor.execute(new PlaceLimitStopOrderRequestModel(
                accountId, symbol, side, type, quantity, trigger));
        return null;
    }
}
