package com.marketmaker.use_case.search_ticker;

import com.marketmaker.entities.Quote;

public class SearchTickerInteractor implements SearchTickerInputBoundary {
    private final TickerDataAccessInterface dataAccess;
    private final SearchTickerOutputBoundary presenter;

    public SearchTickerInteractor(TickerDataAccessInterface dataAccess, SearchTickerOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(SearchTickerRequestModel request) {
        String ticker = request.getTicker();
        if (ticker == null || ticker.isBlank()) {
            presenter.presentFailure("Enter a ticker symbol to search.");
            return;
        }

        Quote quote = dataAccess.fetchQuote(ticker.toUpperCase());
        if (quote == null) {
            presenter.presentFailure("No quote found for ticker " + ticker + ".");
            return;
        }

        presenter.presentSuccess(new SearchTickerResponseModel(
                quote.getTicker(), quote.getPrice(), quote.getTimestamp()));
    }
}
