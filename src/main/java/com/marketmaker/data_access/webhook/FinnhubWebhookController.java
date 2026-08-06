package com.marketmaker.data_access.webhook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/** Receives Finnhub's webhook callbacks over an embedded HTTP server. */
public class FinnhubWebhookController implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(FinnhubWebhookController.class.getName());

    private static final String SECRET_HEADER = "X-Finnhub-Secret";
    private static final String DEFAULT_PATH = "/webhooks/finnhub";

    private final String webhookSecret;
    private final String path;
    private final FinnhubWebhookEventHandler eventHandler;
    private HttpServer server;

    public FinnhubWebhookController(String webhookSecret, FinnhubWebhookEventHandler eventHandler) {
        this(webhookSecret, DEFAULT_PATH, eventHandler);
    }

    public FinnhubWebhookController(String webhookSecret, String path, FinnhubWebhookEventHandler eventHandler) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalArgumentException("webhookSecret must not be null or blank");
        }
        this.webhookSecret = webhookSecret;
        this.path = path;
        this.eventHandler = eventHandler;
    }

    /** Starts an embedded HTTP server on the given port and registers this controller on {@code path}. */
    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(path, this);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        LOGGER.info(() -> "FinnhubWebhookController listening on port " + port + path);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String providedSecret = exchange.getRequestHeaders().getFirst(SECRET_HEADER);
            if (providedSecret == null || !constantTimeEquals(providedSecret, webhookSecret)) {
                sendResponse(exchange, 401, "Unauthorized");
                return;
            }

            String body = readBody(exchange.getRequestBody());
            JSONObject payload = new JSONObject(body);
            dispatch(payload);

            sendResponse(exchange, 200, "OK");
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Error handling Finnhub webhook", exception);
            sendResponse(exchange, 500, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }

    private void dispatch(JSONObject payload) {
        // Finnhub payloads generally look like: {"event": "...", "data": [ ... ]}
        String eventType = payload.optString("event", "unknown");
        JSONArray data = payload.optJSONArray("data");

        if (eventHandler != null) {
            eventHandler.onWebhookEvent(eventType, payload, data);
        }
    }

    private static String readBody(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /** Constant-time string comparison so secret checks don't leak timing info. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /** Decouples this controller from whatever consumes the events. */
    @FunctionalInterface
    public interface FinnhubWebhookEventHandler {
        void onWebhookEvent(String eventType, JSONObject fullPayload, JSONArray data);
    }
}
