package com.marketmaker.use_case.save_account_data;

import com.marketmaker.entities.Account;

/** Contract for durable storage that persists a full account snapshot. */
public interface SaveAccountDataAccessInterface {
    void save(Account account);
}
