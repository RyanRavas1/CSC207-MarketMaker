package com.marketmaker.data_access;

public class FinnhubApiRateLimitException extends FinnhubApiException {
    public FinnhubApiRateLimitException(String message) {
        super(message);
    }
}
