package com.marketmaker.use_case.receive_live_quotes;

import com.marketmaker.entities.Quote;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReceiveLiveQuoteUpdatesInteractorTest {

    private static class FakeLiveQuoteDataAccess implements LiveQuoteDataAccessInterface {
        String subscribedTicker;
        QuoteUpdateListener subscribedListener;
        List<String> unsubscribedTickers = new ArrayList<>();

        @Override
        public void subscribe(String ticker, QuoteUpdateListener listener) {
            this.subscribedTicker = ticker;
            this.subscribedListener = listener;
        }

        @Override
        public void unsubscribe(String ticker) {
            unsubscribedTickers.add(ticker);
        }
    }

    private static class FakePresenter implements ReceiveLiveQuoteUpdatesOutputBoundary {
        final List<LiveQuoteResponseModel> updates = new ArrayList<>();

        @Override
        public void presentQuoteUpdate(LiveQuoteResponseModel response) {
            updates.add(response);
        }
    }

    @Test
    void subscribingForwardsGatewayTicksToPresenter() {
        FakeLiveQuoteDataAccess dataAccess = new FakeLiveQuoteDataAccess();
        FakePresenter presenter = new FakePresenter();
        ReceiveLiveQuoteUpdatesInteractor interactor =
                new ReceiveLiveQuoteUpdatesInteractor(dataAccess, presenter);

        interactor.subscribe("AAPL");
        assertEquals("AAPL", dataAccess.subscribedTicker);

        dataAccess.subscribedListener.onQuote(new Quote("AAPL", 232.50, Instant.EPOCH));

        assertEquals(1, presenter.updates.size());
        assertEquals("AAPL", presenter.updates.get(0).getTicker());
        assertEquals(232.50, presenter.updates.get(0).getPrice());
    }

    @Test
    void unsubscribingDelegatesToGateway() {
        FakeLiveQuoteDataAccess dataAccess = new FakeLiveQuoteDataAccess();
        ReceiveLiveQuoteUpdatesInteractor interactor =
                new ReceiveLiveQuoteUpdatesInteractor(dataAccess, response -> { });

        interactor.unsubscribe("AAPL");

        assertTrue(dataAccess.unsubscribedTickers.contains("AAPL"));
    }
}
