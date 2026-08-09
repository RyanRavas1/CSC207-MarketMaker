package com.marketmaker.data_access;

import com.marketmaker.entities.Account;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryAccountDAOTest {
    @Test void savesRetrievesAndReplacesAccountsByUsername() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account first = new Account("ada", 1); Account replacement = new Account("ada", 2);
        assertNull(dao.get("missing")); dao.save(first); assertSame(first, dao.get("ada")); dao.save(replacement); assertSame(replacement, dao.get("ada"));
    }
}
