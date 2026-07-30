package com.marketmaker.config.exceptions;

public class MissingEnvironmentVariableException extends RuntimeException {
    public MissingEnvironmentVariableException(String key) {
        super("Missing required environment variable: " + key);
    }
}
