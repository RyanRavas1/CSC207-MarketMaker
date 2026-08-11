package com.marketmaker.price_feed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.marketmaker.data_access.FinnhubApiClient;
import com.marketmaker.data_access.FinnhubApiException;
import com.marketmaker.use_case.PriceFeedException;

/** Parsing a /quote body, the call-saving cache, and what happens when the feed misbehaves. */
class FinnhubPriceFeedTest {

    /** Stands in for the HTTP client so no test touches the network. */
    private static FinnhubApiClient clientReturning(String body, AtomicInteger callCount) {
        return new FinnhubApiClient("test-key") {
            @Override
            public String get(String endpointPath, Map<String, String> params) {
                callCount.incrementAndGet();
                return body;
            }
        };
    }

    @Test
    void readsCurrentPriceFromQuoteBody() {
        AtomicInteger calls = new AtomicInteger();
        FinnhubPriceFeed feed = new FinnhubPriceFeed(
                clientReturning("{\"c\":191.25,\"h\":192.0,\"l\":188.5,\"pc\":190.0}", calls));

        assertEquals(191.25, feed.getQuote("AAPL").getPrice());
        assertEquals("AAPL", feed.getQuote("AAPL").getTicker());
    }

    @Test
    void repeatQuotesInsideTheTtlReuseOneApiCall() {
        AtomicInteger calls = new AtomicInteger();
        FinnhubPriceFeed feed = new FinnhubPriceFeed(clientReturning("{\"c\":191.25}", calls));

        feed.getQuote("AAPL");
        feed.getQuote("AAPL");
        feed.getQuote("AAPL");

        assertEquals(1, calls.get()); // three callers, one call against the rate limit
    }

    @Test
    void zeroedBodyMeansUnknownSymbolNotAFreeStock() {
        AtomicInteger calls = new AtomicInteger();
        FinnhubPriceFeed feed = new FinnhubPriceFeed(
                clientReturning("{\"c\":0,\"h\":0,\"l\":0,\"pc\":0}", calls));

        assertThrows(PriceFeedException.class, () -> feed.getQuote("NOTATICKER"));
    }

    @Test
    void apiFailureSurfacesAsAPriceFeedException() {
        FinnhubPriceFeed feed = new FinnhubPriceFeed(new FinnhubApiClient("test-key") {
            @Override
            public String get(String endpointPath, Map<String, String> params) {
                throw new FinnhubApiException("Finnhub API returned status 429: slow down");
            }
        });

        assertThrows(PriceFeedException.class, () -> feed.getQuote("AAPL"));
    }
}
