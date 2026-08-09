package com.marketmaker.use_case;

/**
 * A quote couldn't be fetched. Lives here rather than in data_access so use cases can
 * catch it without importing anything from the layer below them.
 */
public class PriceFeedException extends RuntimeException {
    public PriceFeedException(String message) {
        super(message);
    }

    public PriceFeedException(String message, Throwable cause) {
        super(message, cause);
    }
}
