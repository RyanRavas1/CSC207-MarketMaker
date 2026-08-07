package com.marketmaker.data_access.exceptions;

/** Thrown when historical data cannot be fetched or parsed. */
public class StockDataException extends RuntimeException {
    public StockDataException(String message) {
            super(message);
        }

    public StockDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
