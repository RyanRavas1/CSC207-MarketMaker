package com.marketmaker.config;

import com.marketmaker.config.exceptions.MissingEnvironmentVariableException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class EnvLoader {
    private EnvLoader() {}
    public static String get(String key) {


        // read from local .env file for dev
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2 && parts[0].trim().equals(key)) {
                    return parts[1].trim();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read .env file", e);
        }
        throw new MissingEnvironmentVariableException(key);
    }
}
