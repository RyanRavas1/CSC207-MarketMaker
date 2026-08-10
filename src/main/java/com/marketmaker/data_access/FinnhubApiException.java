package com.marketmaker.data_access;

/**
 * Thrown when a call to the Finnhub API fails - either a non-2xx response
 * or a network-level failure (timeout, connection refused, etc.).
 */
public class FinnhubApiException extends RuntimeException {

    public FinnhubApiException(String message) {
        super(message);
    }

    public FinnhubApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
