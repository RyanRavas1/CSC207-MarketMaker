package com.marketmaker.use_case.create_account;

import com.marketmaker.use_case.AccountDAO;
import com.marketmaker.entities.Account;

/** Sets up a new paper-trading account with a fixed starting cash balance. */
public class CreateAccountInteractor implements CreateAccountInputBoundary {
    private static final double STARTING_BALANCE = 100_000.0;

    private final AccountDAO accountDAO;
    private final CreateAccountOutputBoundary presenter;

    public CreateAccountInteractor(AccountDAO accountDAO, CreateAccountOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.presenter = presenter;
    }

    @Override
    public void execute(CreateAccountRequestModel request) {
        if (accountDAO.get(request.getAccountId()) != null) {
            presenter.presentFailure("Account already exists.");
            return;
        }

        Account account = new Account(request.getAccountId(), STARTING_BALANCE);
        accountDAO.save(account);

        presenter.presentSuccess(new CreateAccountResponseModel(request.getAccountId(), STARTING_BALANCE));
    }
}
