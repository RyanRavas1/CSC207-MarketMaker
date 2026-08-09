package com.marketmaker.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PositionTest {
    @Test void retainsValidPositionAndClampsNegativeShares() {
        Position valid = new Position("AAPL", 3, 12.5); Position invalid = new Position("MSFT", -1, 4);
        assertAll(() -> assertEquals("AAPL", valid.getTicker()), () -> assertEquals(3, valid.getShares()), () -> assertEquals(12.5, valid.getAveragePrice()), () -> assertEquals(0, invalid.getShares()));
    }
}
