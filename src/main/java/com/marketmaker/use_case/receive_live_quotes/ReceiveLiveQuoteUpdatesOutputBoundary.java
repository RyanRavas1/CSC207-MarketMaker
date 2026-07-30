package com.marketmaker.use_case.receive_live_quotes;

public interface ReceiveLiveQuoteUpdatesOutputBoundary {
    void presentQuoteUpdate(LiveQuoteResponseModel response);
}
