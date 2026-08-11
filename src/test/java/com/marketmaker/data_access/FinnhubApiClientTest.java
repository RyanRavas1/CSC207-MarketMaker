package com.marketmaker.data_access;

import org.junit.jupiter.api.Test;

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
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class FinnhubApiClientTest {

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

    @Test
    void getWithParamsSuccessfulResponse() {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.statusCode = 200;
        stubClient.responseBody = "{\"c\":150.0}";

        FinnhubApiClient client = new FinnhubApiClient("test-api-key", stubClient);
        String result = client.get("quote", Map.of("symbol", "AAPL"));

        assertEquals("{\"c\":150.0}", result);
        assertNotNull(stubClient.lastRequest);
        assertEquals("test-api-key", stubClient.lastRequest.headers().firstValue("X-Finnhub-Token").orElse(null));
        assertTrue(stubClient.lastRequest.uri().toString().contains("symbol=AAPL"));
    }

    @Test
    void getWithoutParamsSuccessfulResponse() {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.statusCode = 200;
        stubClient.responseBody = "{\"status\":\"ok\"}";

        FinnhubApiClient client = new FinnhubApiClient("test-api-key", stubClient);
        String result = client.get("/status", null);

        assertEquals("{\"status\":\"ok\"}", result);
        assertTrue(stubClient.lastRequest.uri().toString().endsWith("/api/v1/status"));
    }

    @Test
    void rateLimitReturns429ThrowsRateLimitException() {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.statusCode = 429;
        stubClient.responseBody = "Rate limit exceeded";

        FinnhubApiClient client = new FinnhubApiClient("test-api-key", stubClient);
        Map<String, String> params = Map.of("symbol", "AAPL");
        FinnhubApiRateLimitException ex = assertThrows(FinnhubApiRateLimitException.class, () ->
                client.get("/quote", params));

        assertTrue(ex.getMessage().contains("429"));
    }

    @Test
    void non200Or429StatusCodeThrowsApiException() {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.statusCode = 500;
        stubClient.responseBody = "Internal Server Error";

        FinnhubApiClient client = new FinnhubApiClient("test-api-key", stubClient);
        Map<String, String> params = Map.of("symbol", "AAPL");
        FinnhubApiException ex = assertThrows(FinnhubApiException.class, () ->
                client.get("/quote", params));

        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    void ioExceptionThrowsApiException() {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.ioExceptionToThrow = new IOException("Connection reset");

        FinnhubApiClient client = new FinnhubApiClient("test-api-key", stubClient);
        Map<String, String> params = Map.of("symbol", "AAPL");
        FinnhubApiException ex = assertThrows(FinnhubApiException.class, () ->
                client.get("/quote", params));

        assertTrue(ex.getMessage().contains("Failed to reach Finnhub API"));
    }

    @Test
    void interruptedIOExceptionThrowsApiExceptionAndRestoresInterrupt() {
        StubHttpClient stubClient = new StubHttpClient();
        stubClient.interruptedExceptionToThrow = new InterruptedException("Interrupted");

        FinnhubApiClient client = new FinnhubApiClient("test-api-key", stubClient);
        Map<String, String> params = Map.of("symbol", "AAPL");
        FinnhubApiException ex = assertThrows(FinnhubApiException.class, () ->
                client.get("/quote", params));

        assertTrue(ex.getMessage().contains("Failed to reach Finnhub API"));
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    @Test
    void constructorWithOnlyKeyCreatesDefaultClient() {
        FinnhubApiClient client = new FinnhubApiClient("my-key");
        assertNotNull(client);
    }
}
