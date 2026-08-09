package com.marketmaker.data_access;

import java.util.HashMap;
import java.util.Map;

import com.marketmaker.entities.Account;
import com.marketmaker.use_case.AccountDAO;

/** In-memory account store that is backed by a HashMap. */
public class InMemoryAccountDAO implements AccountDAO {
    private final Map<String, Account> accounts = new HashMap<>();

    @Override
    public Account get(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void save(Account account) {
        accounts.put(account.getUserName(), account);
    }
}
