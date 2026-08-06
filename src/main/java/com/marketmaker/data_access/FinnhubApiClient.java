package com.marketmaker.data_access;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import com.marketmaker.data_access.exceptions.FinnhubApiException;
import com.marketmaker.data_access.exceptions.FinnhubApiRateLimitException;

/**
 * Handles all raw HTTP communication with the Finnhub API.
 * Only returns string of the JSON response.
 */
public class FinnhubApiClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final String apiKey;
    private final HttpClient httpClient;

    public FinnhubApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    // Constructor for tests, can inject a mock/fake HttpClient
    public FinnhubApiClient(String apiKey, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    /**
     * Makes a generic GET call to any Finnhub REST endpoint with the given
     * query parameters. Example:
     *
     *   client.get("/quote", Map.of("symbol", "AAPL"));
     *   client.get("/stock/candle", Map.of(
     *       "symbol", "AAPL", "resolution", "D", "from", "1590988249", "to", "1591852249"));
     *
     * Returns the raw JSON response body as a String. Callers (data access
     * objects) are responsible for parsing this into DTOs/domain objects.
     */
    public String get(String endpointPath, Map<String, String> params) {
        String url = buildUrl(endpointPath, params);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("X-Finnhub-Token", apiKey)
                .GET()
                .build();

        return send(request);
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) { // valid response status codes
                return response.body();
            } else if (response.statusCode() == 429) { // know error code
                throw new FinnhubApiRateLimitException(
                        "Finnhub API returned status " + response.statusCode() + ": " + response.body());
            } else { // catch all
                throw new FinnhubApiException(
                        "Finnhub API returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FinnhubApiException("Failed to reach Finnhub API", exception);
        } catch (IOException exception) {
            throw new FinnhubApiException("Failed to reach Finnhub API", exception);
        }
    }

    private String buildUrl(String endpointPath, Map<String, String> params) {
        final String PATH_SEPARATOR = "/"; // variable for compliance with SonarQube error java:S1075
        String normalizedPath = endpointPath.startsWith("/") ? endpointPath : PATH_SEPARATOR + endpointPath;

        // The key travels in the X-Finnhub-Token header, never here: a query string ends up in
        // proxy logs, shell history and crash reports, and this one is a live credential.
        StringBuilder query = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (query.length() > 0) {
                    query.append("&");
                }
                query.append(encode(entry.getKey()))
                     .append("=")
                     .append(encode(entry.getValue()));
            }
        }

        try {
            // Base path: https://finnhub.io/api/v1
            // null, not "", so a paramless endpoint doesn't get a dangling "?".
            String queryOrNull = query.length() == 0 ? null : query.toString();
            URI uri = new URI("https", "finnhub.io", "/api/v1" + normalizedPath, queryOrNull, null);
            return uri.toASCIIString();
        } catch (URISyntaxException exception) {
            throw new FinnhubApiException("Failed to build Finnhub API URL", exception);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
