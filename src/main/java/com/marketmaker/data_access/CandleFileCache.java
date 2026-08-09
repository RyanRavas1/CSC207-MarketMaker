package com.marketmaker.data_access;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.marketmaker.entities.Candle;

/**
 * Keeps downloaded price history on disk between runs.
 *
 * <p>Daily bars are settled history - yesterday's close will never change - so re-downloading
 * them on every launch spends a 25-a-day budget on facts already known. A file counts as good
 * for the calendar day it was written: the first chart of the day pays for itself, the rest of
 * that day is free, and tomorrow's first fetch corrects today's still-moving close.
 *
 * <p>This means a wrong series outlives the process that fetched it. Deleting
 * {@code data/candles} is the cure, and costs one API call per ticker to rebuild.
 */
public class CandleFileCache {

    private static final String FETCHED_ON = "fetchedOn";
    private static final String CANDLES = "candles";

    private static final Logger LOGGER = Logger.getLogger(CandleFileCache.class.getName());

    private final Path directory;

    public CandleFileCache(Path directory) {
        this.directory = directory;
    }

    /** @return the stored series when it was written today, or an empty list otherwise */
    public List<Candle> read(String ticker, LocalDate today) {
        Path file = fileFor(ticker);
        if (!Files.exists(file)) {
            return List.of();
        }

        try {
            JSONObject stored = new JSONObject(Files.readString(file, StandardCharsets.UTF_8));
            if (!today.toString().equals(stored.optString(FETCHED_ON))) {
                return List.of();
            }
            return toCandles(ticker, stored.getJSONArray(CANDLES));
        }
        catch (IOException | RuntimeException exception) {
            // A corrupt or half-written file is worth one wasted call, not a crash.
            LOGGER.warning("Ignoring unreadable cache for " + ticker + ": " + exception.getMessage());
            return List.of();
        }
    }

    public void write(String ticker, List<Candle> candles, LocalDate today) {
        JSONArray rows = new JSONArray();
        for (Candle candle : candles) {
            rows.put(new JSONObject()
                    .put("t", candle.getTimestamp().toString())
                    .put("o", candle.getOpen())
                    .put("h", candle.getHigh())
                    .put("l", candle.getLow())
                    .put("c", candle.getClose())
                    .put("v", candle.getVolume()));
        }

        JSONObject document = new JSONObject()
                .put(FETCHED_ON, today.toString())
                .put(CANDLES, rows);

        try {
            Files.createDirectories(directory);
            // Written aside and moved into place, so a crash mid-write cannot leave behind a
            // truncated file that reads as a real but shorter price series.
            Path temporary = fileFor(ticker + ".tmp");
            Files.writeString(temporary, document.toString(), StandardCharsets.UTF_8);
            Files.move(temporary, fileFor(ticker), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException exception) {
            LOGGER.warning("Could not cache " + ticker + ": " + exception.getMessage());
        }
    }

    private List<Candle> toCandles(String ticker, JSONArray rows) {
        List<Candle> candles = new ArrayList<>(rows.length());
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            candles.add(new Candle(ticker, "D",
                    row.getDouble("o"), row.getDouble("h"), row.getDouble("l"),
                    row.getDouble("c"), row.getDouble("v"),
                    LocalDateTime.parse(row.getString("t"))));
        }
        return candles;
    }

    private Path fileFor(String name) {
        return directory.resolve(name + ".json");
    }
}
