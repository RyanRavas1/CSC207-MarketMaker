package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.search_ticker.SearchTickerOutputBoundary;
import com.marketmaker.use_case.search_ticker.SearchTickerResponseModel;

/**
 * Reports whether a symbol is real, and says so on screen when it isn't.
 *
 * <p>Remembers the answer as well as publishing it, because the caller is a controller
 * deciding whether to go on and add the ticker — a decision it has to make before returning.
 */
public class TickerSearchPresenter implements SearchTickerOutputBoundary {
    // Only the error channel is used, so the state type is nobody's business here.
    private final ViewModel<?> screen;
    private boolean found;

    public TickerSearchPresenter(ViewModel<?> screen) {
        this.screen = screen;
    }

    @Override
    public void presentSuccess(SearchTickerResponseModel response) {
        // Nothing to announce: the ticker is about to appear in the table with its price.
        found = true;
    }

    @Override
    public void presentFailure(String errorMessage) {
        found = false;
        screen.setError(errorMessage);
    }

    /** @return whether the last search matched a tradable symbol */
    public boolean found() {
        return found;
    }
}
