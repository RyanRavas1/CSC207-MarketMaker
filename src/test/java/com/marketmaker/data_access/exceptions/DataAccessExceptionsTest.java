package com.marketmaker.data_access.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DataAccessExceptionsTest {
    @Test void preserveMessagesAndCauses() {
        RuntimeException cause = new RuntimeException("cause");
        assertAll(() -> assertEquals("api", new FinnhubApiException("api").getMessage()),
                () -> assertSame(cause, new FinnhubApiException("api", cause).getCause()),
                () -> assertEquals("limited", new FinnhubApiRateLimitException("limited").getMessage()),
                () -> assertSame(cause, new StockDataException("stock", cause).getCause()),
                () -> assertSame(cause, new AccountPersistenceException("disk", cause).getCause()));
    }
}
