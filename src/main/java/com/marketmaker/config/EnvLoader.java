package com.marketmaker.config;

import com.marketmaker.config.exceptions.MissingEnvironmentVariableException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {
//  Map of local env file key-value pairs
    private static final Map<String, String> CACHE = loadEnvFile();

    private EnvLoader() {}

    private static Map<String, String> loadEnvFile() {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue; // skip blanks/comments
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read .env file", e);
        }
        return map;
    }

    public static String get(String key) {
//      Check real environment variables first,
//      then check local file as a backup
        String fromEnv = System.getenv(key);
        if (fromEnv != null) return fromEnv;

        String fromFile = CACHE.get(key);
        if (fromFile != null) return fromFile;

        throw new MissingEnvironmentVariableException(key);
    }
}
