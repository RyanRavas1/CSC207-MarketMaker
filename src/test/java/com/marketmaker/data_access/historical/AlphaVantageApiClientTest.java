package com.marketmaker.data_access.historical;

import com.marketmaker.data_access.exceptions.StockDataException;
import com.marketmaker.entities.Candle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AlphaVantageApiClientTest {

    private static class StubHttpResponse implements HttpResponse<String> {
        private final int statusCode;
        private final String body;

        StubHttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        @Override public int statusCode() { return statusCode; }
        @Override public HttpRequest request() { return null; }
        @Override public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
        @Override public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
        @Override public String body() { return body; }
        @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
        @Override public URI uri() { return null; }
        @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
    }

    private static class StubHttpClient extends HttpClient {
        int statusCode = 200;
        String responseBody = "{}";
        IOException ioExceptionToThrow;
        InterruptedException interruptedExceptionToThrow;
        HttpRequest lastRequest;

        @SuppressWarnings("unchecked")
        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
                throws IOException, InterruptedException {
            this.lastRequest = request;
            if (ioExceptionToThrow != null) {
                throw ioExceptionToThrow;
            }
            if (interruptedExceptionToThrow != null) {
                throw interruptedExceptionToThrow;
            }
            return (HttpResponse<T>) new StubHttpResponse(statusCode, responseBody);
        }

        @Override public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
        @Override public Optional<Duration> connectTimeout() { return Optional.empty(); }
        @Override public Redirect followRedirects() { return Redirect.NEVER; }
        @Override public Optional<ProxySelector> proxy() { return Optional.empty(); }
        @Override public SSLContext sslContext() { return null; }
        @Override public SSLParameters sslParameters() { return null; }
        @Override public Optional<Authenticator> authenticator() { return Optional.empty(); }
        @Override public Version version() { return Version.HTTP_1_1; }
        @Override public Optional<Executor> executor() { return Optional.empty(); }
        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                                HttpResponse.BodyHandler<T> responseBodyHandler) {
            return null;
        }
        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                                HttpResponse.BodyHandler<T> responseBodyHandler,
                                                                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            return null;
        }
    }

    private static final String SAMPLE_JSON = """
            {
                "Time Series (5min)": {
                    "2024-03-01 14:35:00": {
                        "1. open": "180.50",
                        "2. high": "180.63",
                        "3. low": "180.45",
                        "4. close": "180.58",
                        "5. volume": "15000"
                    },
                    "2024-03-01 14:30:00": {
                        "1. open": "180.00",
                        "2. high": "180.50",
                        "3. low": "179.90",
                        "4. close": "180.45",
                        "5. volume": "12000"
                    }
                }
            }
            """;

    @Test
    void rejectsNullOrBlankApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new AlphaVantageApiClient(null));
        assertThrows(IllegalArgumentException.class, () -> new AlphaVantageApiClient("   "));
    }

    @Test
    void intervalValues() {
        assertEquals("1min", AlphaVantageApiClient.Interval.ONE_MINUTE.apiValue());
        assertEquals("5min", AlphaVantageApiClient.Interval.FIVE_MINUTES.apiValue());
        assertEquals("15min", AlphaVantageApiClient.Interval.FIFTEEN_MINUTES.apiValue());
        assertEquals("30min", AlphaVantageApiClient.Interval.THIRTY_MINUTES.apiValue());
        assertEquals("60min", AlphaVantageApiClient.Interval.SIXTY_MINUTES.apiValue());
    }

    @Test
    void fetchesFromApiAndCachesResponseOnCacheMiss(@TempDir Path tempDir) throws StockDataException {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.statusCode = 200;
        stubClient.responseBody = SAMPLE_JSON;

        AlphaVantageApiClient client = new AlphaVantageApiClient("demo_key", stubClient, tempDir.toString());
        LocalDateTime requested = LocalDateTime.of(2024, Month.MARCH, 1, 14, 35, 0);

        Optional<Candle> candleOpt = client.findPriceOnOrBefore("aapl", requested,
                AlphaVantageApiClient.Interval.FIVE_MINUTES);

        assertTrue(candleOpt.isPresent());
        Candle candle = candleOpt.get();
        assertEquals("AAPL", candle.getTicker());
        assertEquals(180.50, candle.getOpen());
        assertEquals(180.58, candle.getClose());
        assertEquals(15000.0, candle.getVolume());
        assertEquals(requested, candle.getTimestamp());

        Path expectedCacheFile = tempDir.resolve("AAPL_5min_2024-03.json");
        assertTrue(Files.exists(expectedCacheFile));
    }

    @Test
    void readsFromCacheOnCacheHit(@TempDir Path tempDir) throws Exception {
        Path cacheFile = tempDir.resolve("AAPL_5min_2024-03.json");
        Files.writeString(cacheFile, SAMPLE_JSON);

        StubHttpClient stubClient = new StubHttpClient();
        AlphaVantageApiClient client = new AlphaVantageApiClient("demo_key", stubClient, tempDir.toString());

        LocalDateTime requested = LocalDateTime.of(2024, Month.MARCH, 1, 14, 32, 0); // between 14:30 and 14:35
        Optional<Candle> candleOpt = client.findPriceOnOrBefore("AAPL", requested, AlphaVantageApiClient.Interval.FIVE_MINUTES);

        assertTrue(candleOpt.isPresent());
        // Nearest before 14:32:00 is 14:30:00
        assertEquals(LocalDateTime.of(2024, Month.MARCH, 1, 14, 30, 0), candleOpt.get().getTimestamp());
        assertNull(stubClient.lastRequest); // Did not make HTTP call
    }

    @Test
    void returnsEmptyOptionalWhenNoCandleOnOrBeforeRequested(@TempDir Path tempDir) throws StockDataException {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.responseBody = SAMPLE_JSON;

        AlphaVantageApiClient client = new AlphaVantageApiClient("demo_key", stubClient, tempDir.toString());
        LocalDateTime earlyDate = LocalDateTime.of(2024, Month.MARCH, 1, 14, 0, 0);

        Optional<Candle> candleOpt = client.findPriceOnOrBefore("AAPL", earlyDate, AlphaVantageApiClient.Interval.FIVE_MINUTES);

        assertTrue(candleOpt.isEmpty());
    }

    @Test
    void handlesApiErrorMessage(@TempDir Path tempDir) {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.responseBody = "{\"Error Message\": \"Invalid API call.\"}";

        AlphaVantageApiClient client = new AlphaVantageApiClient("demo_key", stubClient, tempDir.toString());
        LocalDateTime requested = LocalDateTime.of(2024, Month.APRIL, 1, 12, 0, 0);

        StockDataException ex = assertThrows(StockDataException.class, () ->
                client.findPriceOnOrBefore("INVALID", requested, AlphaVantageApiClient.Interval.ONE_MINUTE));

        assertTrue(ex.getMessage().contains("rejected the request"));
    }

    @Test
    void handlesApiRateLimitNote(@TempDir Path tempDir) {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.responseBody = "{\"Note\": \"Thank you for using Alpha Vantage! Call frequency cap 25 requests/day.\"}";

        AlphaVantageApiClient client = new AlphaVantageApiClient("demo_key", stubClient, tempDir.toString());
        LocalDateTime requested = LocalDateTime.of(2024, Month.APRIL, 1, 12, 0, 0);

        StockDataException ex = assertThrows(StockDataException.class, () ->
                client.findPriceOnOrBefore("AAPL", requested, AlphaVantageApiClient.Interval.ONE_MINUTE));

        assertTrue(ex.getMessage().contains("rate limit or entitlement issue"));
    }

    @Test
    void handlesNon200HttpResponse(@TempDir Path tempDir) {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.statusCode = 404;
        stubClient.responseBody = "Not Found";

        AlphaVantageApiClient client = new AlphaVantageApiClient("demo_key", stubClient, tempDir.toString());
        LocalDateTime requested = LocalDateTime.of(2024, Month.APRIL, 1, 12, 0, 0);

        StockDataException ex = assertThrows(StockDataException.class, () ->
                client.findPriceOnOrBefore("AAPL", requested, AlphaVantageApiClient.Interval.ONE_MINUTE));

        assertTrue(ex.getMessage().contains("HTTP 404"));
    }

    @Test
    void handlesNetworkIOException(@TempDir Path tempDir) {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.ioExceptionToThrow = new IOException("Network unreachable");

        AlphaVantageApiClient client = new AlphaVantageApiClient("demo_key", stubClient, tempDir.toString());
        LocalDateTime requested = LocalDateTime.of(2024, Month.APRIL, 1, 12, 0, 0);

        StockDataException ex = assertThrows(StockDataException.class, () ->
                client.findPriceOnOrBefore("AAPL", requested, AlphaVantageApiClient.Interval.ONE_MINUTE));

        assertTrue(ex.getMessage().contains("Network error fetching data"));
    }
}
