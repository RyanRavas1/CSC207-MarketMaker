package com.marketmaker.use_case.receive_live_quotes;

public interface ReceiveLiveQuoteUpdatesInputBoundary {
    void subscribe(String ticker);

    void unsubscribe(String ticker);
}
