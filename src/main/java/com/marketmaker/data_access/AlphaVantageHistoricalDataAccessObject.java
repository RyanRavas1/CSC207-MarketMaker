package com.marketmaker.data_access;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.marketmaker.entities.Candle;
import com.marketmaker.use_case.view_trend_chart.HistoricalDataAccessInterface;
import com.marketmaker.use_case.view_trend_chart.HistoricalDataUnavailableException;
import com.marketmaker.use_case.view_trend_chart.Resolution;

/**
 * Daily closing prices from Alpha Vantage.
 *
 * <p>Daily bars rather than intraday ones because the intraday endpoint is a paid one; a free
 * key sees one price per trading day. One request returns about a hundred days, so both
 * ranges are served from the same response and the range buttons cost nothing.
 *
 * <p>The cache is what makes this usable at all: a free key allows 25 requests a day and the
 * dashboard refreshes every ten seconds, which would exhaust the quota in four minutes.
 * Holding each ticker for an hour keeps a whole session inside a handful of requests, and
 * costs nothing in freshness - the series only gains a point once a day.
 *
 * <p>Failures are cached too, for a shorter spell. A ticker that fails is usually failing
 * because the quota is gone, and retrying it every refresh spends the little that is left on
 * finding out it is still gone. Failures stay in memory only - a restart should clear the
 * belief that the quota is gone, since it may well have rolled over.
 *
 * <p>Successes also go to disk, so a restart costs nothing. See {@link CandleFileCache}.
 */
public class AlphaVantageHistoricalDataAccessObject implements HistoricalDataAccessInterface {

    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private static final String SERIES_KEY = "Time Series (Daily)";
    private static final Duration CACHE_TTL = Duration.ofHours(1);
    // Long enough that a rate limit clears, short enough that a passing outage isn't fatal.
    private static final Duration FAILURE_TTL = Duration.ofMinutes(5);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    // Alpha Vantage asks for no more than one request a second and answers with prose rather
    // than a 429 when you ignore that, so the spacing is kept here rather than discovered.
    private static final long MIN_GAP_MS = 1200;

    private static final Logger LOGGER =
            Logger.getLogger(AlphaVantageHistoricalDataAccessObject.class.getName());

    private record CachedSeries(List<Candle> candles, Instant fetchedAt) { }

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String apiKey;
    private final CandleFileCache files;
    private final Map<String, CachedSeries> cache = new ConcurrentHashMap<>();
    private Instant lastRequest = Instant.EPOCH;

    public AlphaVantageHistoricalDataAccessObject(String apiKey, CandleFileCache files) {
        this.apiKey = apiKey;
        this.files = files;
    }

    @Override
    public List<Candle> fetchCandles(String ticker, Resolution resolution) {
        List<Candle> series = seriesFor(ticker);
        if (series.isEmpty()) {
            return List.of();
        }

        // The series is oldest-first, so the requested span is its tail.
        int wanted = Math.min(resolution.getTradingDays(), series.size());
        return List.copyOf(series.subList(series.size() - wanted, series.size()));
    }

    private synchronized List<Candle> seriesFor(String ticker) {
        CachedSeries cached = cache.get(ticker);
        if (cached != null && Duration.between(cached.fetchedAt(), Instant.now()).compareTo(ttlOf(cached)) < 0) {
            return cached.candles();
        }

        // Disk before network: yesterday's bars are already final, and today's were already
        // paid for if this is not the first launch of the day.
        LocalDate today = LocalDate.now();
        List<Candle> stored = files.read(ticker, today);
        if (!stored.isEmpty()) {
            cache.put(ticker, new CachedSeries(stored, Instant.now()));
            return stored;
        }

        pace();
        List<Candle> fetched;
        try {
            fetched = download(ticker);
        }
        catch (HistoricalDataUnavailableException exception) {
            // Remembered as a failure so the next refresh doesn't spend another call learning
            // the same thing, then re-raised so the user is told why the chart is empty.
            cache.put(ticker, new CachedSeries(List.of(), Instant.now()));
            throw exception;
        }

        cache.put(ticker, new CachedSeries(fetched, Instant.now()));
        if (!fetched.isEmpty()) {
            files.write(ticker, fetched, today);
        }
        return fetched;
    }

    private static Duration ttlOf(CachedSeries cached) {
        return cached.candles().isEmpty() ? FAILURE_TTL : CACHE_TTL;
    }

    /** Holds the caller back until this request is far enough from the last one. */
    private void pace() {
        long sinceLast = Duration.between(lastRequest, Instant.now()).toMillis();
        if (sinceLast < MIN_GAP_MS) {
            try {
                Thread.sleep(MIN_GAP_MS - sinceLast);
            }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequest = Instant.now();
    }

    private List<Candle> download(String ticker) {
        String url = String.format("%s?function=TIME_SERIES_DAILY&symbol=%s&outputsize=compact&apikey=%s",
                BASE_URL, ticker, apiKey);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET().build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parse(ticker, response.body());
        }
        catch (IOException exception) {
            LOGGER.warning("Could not reach Alpha Vantage for " + ticker + ": " + exception.getMessage());
            throw new HistoricalDataUnavailableException("Could not reach the price history service.");
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return List.of();
        }
    }

    // Package-private so the parsing can be tested without reaching the network.
    List<Candle> parse(String ticker, String body) {
        JSONObject json = new JSONObject(body);
        // Alpha Vantage answers a throttled or unauthorised request with a 200 and a prose
        // "Information" field, so a missing series is the failure signal, not the status code.
        if (!json.has(SERIES_KEY)) {
            String information = json.optString("Information", "");
            LOGGER.warning("No daily series for " + ticker + ": " + information
                    + json.optString("Error Message", ""));
            // The free tier says so in prose rather than a status code, and the two cases lead
            // the user somewhere different: one is "wait", the other is "check what you typed".
            if (information.contains("rate limit") || information.contains("premium")) {
                throw new HistoricalDataUnavailableException(
                        "Daily price-history limit reached - charts resume tomorrow.");
            }
            return List.of();
        }

        JSONObject series = json.getJSONObject(SERIES_KEY);
        List<String> days = new ArrayList<>(series.keySet());
        // Dates are ISO, so sorting them as text puts the series in order.
        days.sort(String::compareTo);

        List<Candle> candles = new ArrayList<>(days.size());
        for (String day : days) {
            JSONObject bar = series.getJSONObject(day);
            candles.add(new Candle(ticker, "D",
                    bar.getDouble("1. open"),
                    bar.getDouble("2. high"),
                    bar.getDouble("3. low"),
                    bar.getDouble("4. close"),
                    bar.getDouble("5. volume"),
                    LocalDate.parse(day).atStartOfDay()));
        }
        return candles;
    }
}
