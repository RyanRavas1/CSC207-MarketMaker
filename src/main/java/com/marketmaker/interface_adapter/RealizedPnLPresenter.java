package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.calculate_realized_pnl.CalculateRealizedPnLOutputBoundary;
import com.marketmaker.use_case.calculate_realized_pnl.CalculateRealizedPnLResponseModel;
import com.marketmaker.view.Format;

/**
 * Turns a realized profit or loss into the line shown under the order ticket.
 *
 * <p>Holds the message rather than publishing it, because this answers a question the form
 * asks while the user is still typing — there is no other screen waiting on it.
 */
public class RealizedPnLPresenter implements CalculateRealizedPnLOutputBoundary {
    private String message;

    @Override
    public void presentSuccess(CalculateRealizedPnLResponseModel response) {
        message = String.format("Selling %d %s realizes %s",
                response.getQuantitySold(), response.getTicker(),
                Format.signedMoney(response.getRealizedPnL()));
    }

    @Override
    public void presentFailure(String errorMessage) {
        // Nothing to show: the ticket is half-filled or names a stock the user doesn't hold,
        // which is a normal state to be typing through, not an error worth shouting about.
        message = null;
    }

    /** @return the last outcome, or null when there was nothing to say */
    public String getMessage() {
        return message;
    }
}
