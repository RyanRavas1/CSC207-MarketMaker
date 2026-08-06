package com.marketmaker.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.marketmaker.config.exceptions.MissingEnvironmentVariableException;

/** Reads configuration from real environment variables, falling back to a local .env file. */
public final class EnvLoader {
    // Both locations get used in practice: the repo root is the usual convention, and src/
    // is where .env.example sits, so that's where people put theirs. Read whichever exists.
    private static final String[] SEARCH_PATHS = {".env", "src/.env"};

    // Map of local env file key-value pairs.
    private static final Map<String, String> CACHE = loadEnvFile();

    private EnvLoader() {
    }

    public static String get(String key) {
        // Check real environment variables first, then the local file as a backup.
        String fromEnv = System.getenv(key);
        if (fromEnv != null) {
            return fromEnv;
        }

        String fromFile = CACHE.get(key);
        if (fromFile != null) {
            return fromFile;
        }

        throw new MissingEnvironmentVariableException(key);
    }

    private static Map<String, String> loadEnvFile() {
        Map<String, String> map = new HashMap<>();
        for (String path : SEARCH_PATHS) {
            readInto(map, path);
        }
        return map;
    }

    // Earlier paths win, so a root .env overrides src/.env rather than being overwritten by it.
    private static void readInto(Map<String, String> map, String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) { // skip blanks/comments
                    continue;
                }
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    map.putIfAbsent(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException exception) {
            // A missing .env is a normal setup (real environment variables, or a demo run
            // with no key at all). Let get() report the specific key that's missing.
        }
    }
}
