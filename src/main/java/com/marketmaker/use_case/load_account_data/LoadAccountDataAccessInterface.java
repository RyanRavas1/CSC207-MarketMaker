package com.marketmaker.use_case.load_account_data;

import com.marketmaker.entities.Account;

/** Contract for durable storage that restores a full account snapshot. */
public interface LoadAccountDataAccessInterface {
    // returns null if no saved data exists for this account id
    Account load(String accountId);
}
