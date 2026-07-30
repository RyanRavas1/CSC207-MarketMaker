package com.marketmaker.data_access.exceptions;

public class FinnhubApiRateLimitException extends FinnhubApiException {
    public FinnhubApiRateLimitException(String message) {
        super(message);
    }
}
