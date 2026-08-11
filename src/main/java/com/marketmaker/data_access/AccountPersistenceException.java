package com.marketmaker.data_access;

public class AccountPersistenceException extends RuntimeException {
    public AccountPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
