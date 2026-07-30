package com.marketmaker.use_case.load_account_data;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.data_access.exceptions.AccountPersistenceException;
import com.marketmaker.entities.Account;

/** Restores saved account, positions, watchlist, and history when the app reopens. */
public class LoadAccountDataInteractor implements LoadAccountDataInputBoundary {
    private final AccountDAO accountDAO;
    private final LoadAccountDataAccessInterface fileDataAccess;
    private final LoadAccountDataOutputBoundary presenter;

    public LoadAccountDataInteractor(AccountDAO accountDAO, LoadAccountDataAccessInterface fileDataAccess,
                                      LoadAccountDataOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.fileDataAccess = fileDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(LoadAccountDataRequestModel request) {
        Account account;
        try {
            account = fileDataAccess.load(request.getAccountId());
        } catch (AccountPersistenceException e) {
            presenter.presentFailure("Failed to load account data: " + e.getMessage());
            return;
        }

        if (account == null) {
            presenter.presentFailure("No saved data found for account " + request.getAccountId() + ".");
            return;
        }

        accountDAO.save(account);
        presenter.presentSuccess(new LoadAccountDataResponseModel(account.getUserName(), account.getUserBalance()));
    }
}
