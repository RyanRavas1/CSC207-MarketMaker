package com.marketmaker.use_case.receive_live_quotes;

/** Continuously forwards price ticks for subscribed tickers to the presenter while the market is open. */
public class ReceiveLiveQuoteUpdatesInteractor implements ReceiveLiveQuoteUpdatesInputBoundary {
    private final LiveQuoteDataAccessInterface liveQuoteDataAccess;
    private final ReceiveLiveQuoteUpdatesOutputBoundary presenter;

    public ReceiveLiveQuoteUpdatesInteractor(LiveQuoteDataAccessInterface liveQuoteDataAccess,
                                              ReceiveLiveQuoteUpdatesOutputBoundary presenter) {
        this.liveQuoteDataAccess = liveQuoteDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void subscribe(String ticker) {
        liveQuoteDataAccess.subscribe(ticker, quote -> presenter.presentQuoteUpdate(
                new LiveQuoteResponseModel(quote.getTicker(), quote.getPrice(), quote.getTimestamp())));
    }

    @Override
    public void unsubscribe(String ticker) {
        liveQuoteDataAccess.unsubscribe(ticker);
    }
}
