package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.calculate_realized_pnl.CalculateRealizedPnLInputBoundary;
import com.marketmaker.use_case.calculate_realized_pnl.CalculateRealizedPnLRequestModel;

/**
 * Asks what a sale would realize, while the user is still filling in the ticket.
 *
 * <p>Answers straight back rather than through a view model: the form redraws this line on
 * every keystroke, and the use case only reads.
 */
public class RealizedPnLController {
    private final CalculateRealizedPnLInputBoundary interactor;
    private final RealizedPnLPresenter presenter;
    private final String accountId;

    public RealizedPnLController(CalculateRealizedPnLInputBoundary interactor,
                                 RealizedPnLPresenter presenter, String accountId) {
        this.interactor = interactor;
        this.presenter = presenter;
        this.accountId = accountId;
    }

    /** @return what the sale would realize, or null when the ticket can't be priced yet */
    public String estimate(String ticker, int quantity, double salePrice) {
        if (ticker == null || ticker.isBlank()) {
            return null;
        }

        interactor.execute(new CalculateRealizedPnLRequestModel(
                accountId, ticker.trim().toUpperCase(), quantity, salePrice));
        return presenter.getMessage();
    }
}
