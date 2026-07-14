package com.marketmaker.data_access;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import com.marketmaker.data_access.exceptions.FinnhubApiException;
import com.marketmaker.data_access.exceptions.FinnhubApiRateLimitException;

/**
 * Handles all raw HTTP communication with the Finnhub API.
 * This class knows nothing about Order, Position, Trade, etc. — it only
 * knows how to send requests and hand back raw JSON strings. Translating
 * that JSON into domain objects is the job of a separate mapper class.
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

    // Constructor for tests — lets you inject a mock/fake HttpClient
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
            }
            else if (response.statusCode() == 429) { // know error code
                throw new FinnhubApiRateLimitException(
                        "Finnhub API returned status " + response.statusCode() + ": " + response.body());
            }
//          catch all else
            else {
                throw new FinnhubApiException(
                        "Finnhub API returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FinnhubApiException("Failed to reach Finnhub API", e);
        } catch (java.io.IOException e) {
            throw new FinnhubApiException("Failed to reach Finnhub API", e);
        }
    }

    private String buildUrl(String endpointPath, Map<String, String> params) {
        final String PATH_SEPARATOR = "/"; // variable for compliance with SonarQube error java:S1075
        String normalizedPath = endpointPath.startsWith("/") ? endpointPath : PATH_SEPARATOR + endpointPath;

        StringBuilder query = new StringBuilder("token=").append(encode(apiKey));
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                query.append("&")
                     .append(encode(entry.getKey()))
                     .append("=")
                     .append(encode(entry.getValue()));
            }
        }

        try {
//          Base path: https://finnhub.io/api/v1
            URI uri = new URI("https", "finnhub.io", "/api/v1" + normalizedPath, query.toString(), null);
            return uri.toASCIIString();
        } catch (java.net.URISyntaxException e) {
            throw new FinnhubApiException("Failed to build Finnhub API URL", e);
        }
}

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
