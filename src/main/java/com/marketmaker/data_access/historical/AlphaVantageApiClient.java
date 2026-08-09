package com.marketmaker.data_access.historical;

import com.marketmaker.data_access.exceptions.StockDataException;
import com.marketmaker.entities.Candle;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches historical intraday stock prices from Alpha Vantage and finds the
 * price of a given US ticker at (or nearest before) a specific date and time.
 *
 * Timestamps are interpreted in US Eastern Time (the exchange's local time),
 * which is what Alpha Vantage returns them in.
 */
public class AlphaVantageApiClient {
    private static final String CACHE_DIR = "av-cache";
    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private static final DateTimeFormatter AV_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM");

    // Matches one Alpha Vantage intraday entry,
    // Example entry:
    // "2024-03-01 14:35:00": {"1. open":"180.50","2. high":"180.63","3. low":"180.45",
    //                         "4. close":"180.58","5. volume":"15000"}
    private static final Pattern ENTRY_PATTERN = Pattern.compile(
            "\"(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\"\\s*:\\s*\\{"
                    + "\\s*\"1\\. open\"\\s*:\\s*\"([\\d.]+)\"\\s*,\\s*"
                    + "\"2\\. high\"\\s*:\\s*\"([\\d.]+)\"\\s*,\\s*"
                    + "\"3\\. low\"\\s*:\\s*\"([\\d.]+)\"\\s*,\\s*"
                    + "\"4\\. close\"\\s*:\\s*\"([\\d.]+)\"\\s*,\\s*"
                    + "\"5\\. volume\"\\s*:\\s*\"(\\d+)\"\\s*\\}"
    );

    private final HttpClient httpClient;
    private final String apiKey;

    public AlphaVantageApiClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be null or blank");
        }
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = apiKey;
    }

    /**
     * Supported Alpha Vantage intraday bar intervals.
     */
    public enum Interval {
        ONE_MINUTE("1min"),
        FIVE_MINUTES("5min"),
        FIFTEEN_MINUTES("15min"),
        THIRTY_MINUTES("30min"),
        SIXTY_MINUTES("60min");

        private final String apiValue;

        Interval(String apiValue) {
            this.apiValue = apiValue;
        }

        public String apiValue() { return apiValue; }
    }

    /**
     * Returns the intraday bar for the given ticker at the requested moment,
     * or the most recent bar before it within the same trading month if there's
     * no exact match (bars land on fixed interval boundaries, e.g. :00/:05/:10...).
     *
     * @param ticker    US equity symbol, e.g. "AAPL"
     * @param requested moment to look up, in US Eastern Time
     * @param interval  bar granularity to request from the API
     */
    public Optional<Candle> findPriceOnOrBefore(String ticker, LocalDateTime requested, Interval interval)
            throws StockDataException {
        TreeMap<LocalDateTime, Candle> monthBars = getMonthBars(ticker, requested.toLocalDate(), interval);
        return Optional.ofNullable(monthBars.floorEntry(requested)).map(Map.Entry::getValue);
    }

    /**
     * Returns all intraday bars for the given ticker's month, using the local
     * cache if present, otherwise fetching from Alpha Vantage and caching the
     * raw response for next time.
     */
    private TreeMap<LocalDateTime, Candle> getMonthBars(String ticker, LocalDate anyDayInMonth, Interval interval)
            throws StockDataException {
        String normalizedTicker = ticker.toUpperCase(Locale.ROOT);
        String month = anyDayInMonth.format(MONTH_FORMAT);
        Path cacheFile = cacheFilePath(normalizedTicker, month, interval);

        String json;
        try {
            if (Files.exists(cacheFile)) {
                json = Files.readString(cacheFile, StandardCharsets.UTF_8);
            } else {
                json = fetchFromApi(normalizedTicker, month, interval);
                Files.createDirectories(cacheFile.getParent());
                Files.writeString(cacheFile, json, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new StockDataException("Failed to read/write cache for " + normalizedTicker + " " + month, e);
        }

        checkForApiErrors(json, normalizedTicker);
        return parseCandles(json, normalizedTicker, interval);
    }

    private String fetchFromApi(String ticker, String month, Interval interval) throws StockDataException {
        String url = String.format(
                "%s?function=TIME_SERIES_INTRADAY&symbol=%s&interval=%s&month=%s&outputsize=full&apikey=%s",
                BASE_URL, ticker, interval.apiValue(), month, apiKey);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                throw new StockDataException(
                        "HTTP " + response.statusCode() + " from Alpha Vantage for ticker " + ticker);
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StockDataException("Network error fetching data for " + ticker, e);
        }
    }

    private void checkForApiErrors(String json, String ticker) throws StockDataException {
        if (json.contains("\"Error Message\"")) {
            throw new StockDataException("Alpha Vantage rejected the request for " + ticker + " (invalid symbol?)");
        }
        if (json.contains("\"Note\"") || json.contains("\"Information\"")) {
            throw new StockDataException("Alpha Vantage rate limit or entitlement issue for " + ticker
                    + " -- you may have hit the 25 requests/day free-tier cap");
        }
    }

    private TreeMap<LocalDateTime, Candle> parseCandles(String json, String ticker, Interval interval) {
        TreeMap<LocalDateTime, Candle> candles = new TreeMap<>();
        Matcher matcher = ENTRY_PATTERN.matcher(json);

        while (matcher.find()) {
            LocalDateTime timestamp = LocalDateTime.parse(matcher.group(1), AV_TIMESTAMP_FORMAT);
            Candle candle = new Candle(
                    ticker,
                    interval.apiValue(),
                    Double.parseDouble(matcher.group(2)),
                    Double.parseDouble(matcher.group(3)),
                    Double.parseDouble(matcher.group(4)),
                    Double.parseDouble(matcher.group(5)),
                    Double.parseDouble(matcher.group(6)),
                    timestamp
            );
            candles.put(timestamp, candle);
        }

        return candles;
    }

    private Path cacheFilePath(String ticker, String month, Interval interval) {
        return Paths.get(CACHE_DIR, ticker + "_" + interval.apiValue() + "_" + month + ".json");
    }
}
