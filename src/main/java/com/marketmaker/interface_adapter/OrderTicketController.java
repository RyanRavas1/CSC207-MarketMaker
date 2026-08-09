package com.marketmaker.interface_adapter;

import java.util.concurrent.Executor;

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
    private final Executor worker;
    private final String accountId;

    public OrderTicketController(PlaceOrderInputBoundary marketInteractor,
                                 PlaceLimitStopOrderInputBoundary restingInteractor,
                                 Executor worker, String accountId) {
        this.marketInteractor = marketInteractor;
        this.restingInteractor = restingInteractor;
        this.worker = worker;
        this.accountId = accountId;
    }

    /**
     * Reads the ticket and sends it to the use case for that order type.
     *
     * <p>Every field the chosen order type needs is validated before anything is
     * dispatched, so a ticket is either fully readable and sent, or rejected with nothing
     * having run. The trigger field is only read for limit and stop orders, because it is
     * not part of a market order: validating it for every order type would reject a valid
     * market ticket over stale text left in a box that does not apply to it.
     *
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
        final int quantity;
        try {
            quantity = Integer.parseInt(quantityText.trim());
        } catch (NumberFormatException exception) {
            return "Quantity must be a whole number.";
        }

        final boolean resting = type != Order.Type.MARKET;
        double trigger = 0.0;
        if (resting) {
            try {
                trigger = Double.parseDouble(triggerText.trim());
            } catch (NumberFormatException exception) {
                return type == Order.Type.LIMIT ? "Enter a limit price." : "Enter a stop price.";
            }
        }

        // Past this point the ticket is known to be readable, so exactly one dispatch runs.
        // Reading the ticket stays on the calling thread because the caller wants the reason
        // back; placing the order does not, because it writes the account. Everything that
        // writes the account runs on the one worker - see WatchlistController - so a save can
        // never carry a copy of the account taken before somebody else's save.
        final double triggerPrice = trigger;
        worker.execute(() -> {
            if (resting) {
                restingInteractor.execute(new PlaceLimitStopOrderRequestModel(
                        accountId, symbol, side, type, quantity, triggerPrice));
            } else {
                marketInteractor.execute(new PlaceOrderRequestModel(accountId, symbol, side, quantity));
            }
        });
        return null;
    }
}
