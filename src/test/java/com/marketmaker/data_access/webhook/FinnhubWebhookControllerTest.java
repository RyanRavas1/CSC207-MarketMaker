package com.marketmaker.data_access.webhook;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class FinnhubWebhookControllerTest {

    private static class StubHttpExchange extends HttpExchange {
        private final String method;
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final InputStream requestBody;
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int responseCode = -1;

        StubHttpExchange(String method, String body) {
            this.method = method;
            this.requestBody = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        }

        @Override public Headers getRequestHeaders() { return requestHeaders; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public URI getRequestURI() { return URI.create("/webhooks/finnhub"); }
        @Override public String getRequestMethod() { return method; }
        @Override public com.sun.net.httpserver.HttpContext getHttpContext() { return null; }
        @Override public void close() {
            // No resources to release; this stub does not open any underlying connection.
        }
        @Override public InputStream getRequestBody() { return requestBody; }
        @Override public OutputStream getResponseBody() { return responseBody; }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
            this.responseCode = rCode;
        }

        @Override public InetSocketAddress getRemoteAddress() { return null; }
        @Override public int getResponseCode() { return responseCode; }
        @Override public InetSocketAddress getLocalAddress() { return null; }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {
            // Attribute storage is not exercised by these tests; nothing to persist.
        }
        @Override public void setStreams(InputStream i, OutputStream o) {
            // Stream substitution is not needed; the stub already exposes fixed
            // request/response streams via getRequestBody()/getResponseBody().
        }
        @Override public com.sun.net.httpserver.HttpPrincipal getPrincipal() { return null; }
    }

    @Test
    void rejectsNullOrBlankSecret() {
        assertThrows(IllegalArgumentException.class, () -> new FinnhubWebhookController(null, (e, p, d) -> {}));
        assertThrows(IllegalArgumentException.class, () -> new FinnhubWebhookController("", (e, p, d) -> {}));
    }

    @Test
    void rejectsNonPostMethodWith405() throws Exception {
        FinnhubWebhookController controller = new FinnhubWebhookController("secret123", (e, p, d) -> {});
        StubHttpExchange exchange = new StubHttpExchange("GET", "");
        exchange.getRequestHeaders().set("X-Finnhub-Secret", "secret123");

        controller.handle(exchange);

        assertEquals(405, exchange.getResponseCode());
        assertEquals("Method Not Allowed", exchange.responseBody.toString(StandardCharsets.UTF_8));
    }

    @Test
    void rejectsMissingOrWrongSecretWith401() throws Exception {
        FinnhubWebhookController controller = new FinnhubWebhookController("secret123", (e, p, d) -> {});

        // Missing header
        StubHttpExchange noHeader = new StubHttpExchange("POST", "{}");
        controller.handle(noHeader);
        assertEquals(401, noHeader.getResponseCode());

        // Wrong secret length or content
        StubHttpExchange wrongSecret = new StubHttpExchange("POST", "{}");
        wrongSecret.getRequestHeaders().set("X-Finnhub-Secret", "wrongsecret");
        controller.handle(wrongSecret);
        assertEquals(401, wrongSecret.getResponseCode());
    }

    @Test
    void dispatchesValidPostRequest() throws Exception {
        AtomicBoolean dispatched = new AtomicBoolean(false);
        String secret = "valid_secret_key";
        FinnhubWebhookController controller = new FinnhubWebhookController(secret, (eventType, fullPayload, data) -> {
            dispatched.set(true);
            assertEquals("trade", eventType);
            assertNotNull(data);
            assertEquals(1, data.length());
            assertEquals("AAPL", data.getJSONObject(0).getString("s"));
        });

        JSONObject payload = new JSONObject();
        payload.put("event", "trade");
        JSONArray data = new JSONArray();
        data.put(new JSONObject().put("s", "AAPL").put("p", 150.0));
        payload.put("data", data);

        StubHttpExchange exchange = new StubHttpExchange("POST", payload.toString());
        exchange.getRequestHeaders().set("X-Finnhub-Secret", secret);

        controller.handle(exchange);

        assertEquals(200, exchange.getResponseCode());
        assertTrue(dispatched.get());
    }

    @Test
    void handlesMalformedJsonPayloadWith500() throws Exception {
        FinnhubWebhookController controller = new FinnhubWebhookController("secret", (e, p, d) -> {});
        StubHttpExchange exchange = new StubHttpExchange("POST", "INVALID_JSON");
        exchange.getRequestHeaders().set("X-Finnhub-Secret", "secret");

        controller.handle(exchange);

        assertEquals(500, exchange.getResponseCode());
    }

    @Test
    void startAndStopServerOnRealPort() throws Exception {
        int freePort;
        try (ServerSocket socket = new ServerSocket(0)) {
            freePort = socket.getLocalPort();
        }

        AtomicBoolean eventReceived = new AtomicBoolean(false);
        String secret = "secret_port_test";
        FinnhubWebhookController controller = new FinnhubWebhookController(secret, "/webhooks/finnhub", (e, p, d) -> {
            eventReceived.set(true);
        });

        controller.start(freePort);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + freePort + "/webhooks/finnhub"))
                    .header("X-Finnhub-Secret", secret)
                    .POST(HttpRequest.BodyPublishers.ofString("{\"event\":\"ping\"}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(eventReceived.get());
        } finally {
            controller.stop();
        }
    }
}
