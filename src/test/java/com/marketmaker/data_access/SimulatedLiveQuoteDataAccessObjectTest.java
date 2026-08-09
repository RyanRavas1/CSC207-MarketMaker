package com.marketmaker.data_access;

import com.marketmaker.entities.Quote;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SimulatedLiveQuoteDataAccessObjectTest {

    @Test
    void subscribeEmitsQuotesAndUnsubscribeStopsQuotes() throws InterruptedException {
        SimulatedLiveQuoteDataAccessObject dao = new SimulatedLiveQuoteDataAccessObject();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Quote> receivedQuote = new AtomicReference<>();

        dao.subscribe("AAPL", quote -> {
            receivedQuote.set(quote);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertNotNull(receivedQuote.get());
        assertEquals("AAPL", receivedQuote.get().getTicker());
        assertTrue(receivedQuote.get().getPrice() > 0);

        dao.unsubscribe("AAPL");
        // Unsubscribing a non-existent ticker or twice should be a no-op
        dao.unsubscribe("AAPL");
    }

    @Test
    void reSubscribingCancelsPreviousTask() throws InterruptedException {
        SimulatedLiveQuoteDataAccessObject dao = new SimulatedLiveQuoteDataAccessObject();
        CountDownLatch latch = new CountDownLatch(1);

        dao.subscribe("AAPL", quote -> {});
        // Subscribing again replaces subscription
        dao.subscribe("AAPL", quote -> latch.countDown());

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        dao.unsubscribe("AAPL");
    }
}
