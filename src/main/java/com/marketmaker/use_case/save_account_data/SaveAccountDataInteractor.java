package com.marketmaker.use_case.save_account_data;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.data_access.exceptions.AccountPersistenceException;
import com.marketmaker.entities.Account;

/** Persists account, positions, watchlist, and history to local storage. */
public class SaveAccountDataInteractor implements SaveAccountDataInputBoundary {
    private final AccountDAO accountDAO;
    private final SaveAccountDataAccessInterface fileDataAccess;
    private final SaveAccountDataOutputBoundary presenter;

    public SaveAccountDataInteractor(AccountDAO accountDAO, SaveAccountDataAccessInterface fileDataAccess,
                                      SaveAccountDataOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.fileDataAccess = fileDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(SaveAccountDataRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        try {
            fileDataAccess.save(account);
        } catch (AccountPersistenceException e) {
            presenter.presentFailure("Failed to save account data: " + e.getMessage());
            return;
        }

        presenter.presentSuccess(new SaveAccountDataResponseModel(request.getAccountId()));
    }
}
