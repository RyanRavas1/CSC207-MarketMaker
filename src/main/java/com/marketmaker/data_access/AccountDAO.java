package com.marketmaker.data_access;

import com.marketmaker.entities.Account;

public interface AccountDAO {
    // returns null if no account has been created yet for this id
    Account get(String accountId);

    void save(Account account);
}
