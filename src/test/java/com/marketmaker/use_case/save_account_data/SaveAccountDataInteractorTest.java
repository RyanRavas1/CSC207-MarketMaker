package com.marketmaker.use_case.save_account_data;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.data_access.exceptions.AccountPersistenceException;
import com.marketmaker.entities.Account;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SaveAccountDataInteractorTest {
    private static class Storage implements SaveAccountDataAccessInterface {
        Account saved; RuntimeException exception;
        public void save(Account account) { if (exception != null) throw exception; saved = account; }
    }
    private static class Presenter implements SaveAccountDataOutputBoundary {
        SaveAccountDataResponseModel success; String failure;
        public void presentSuccess(SaveAccountDataResponseModel response) { success = response; }
        public void presentFailure(String errorMessage) { failure = errorMessage; }
    }

    @Test void savesLocatedAccountAndConfirmsSuccess() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account account = new Account("ada", 10); dao.save(account); Storage storage = new Storage(); Presenter presenter = new Presenter();
        new SaveAccountDataInteractor(dao, storage, presenter).execute(new SaveAccountDataRequestModel("ada"));
        assertSame(account, storage.saved); assertEquals("ada", presenter.success.getAccountId()); assertNull(presenter.failure);
    }

    @Test void reportsMissingAccountAndPersistenceFailure() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Storage storage = new Storage(); Presenter presenter = new Presenter(); SaveAccountDataInteractor interactor = new SaveAccountDataInteractor(dao, storage, presenter);
        interactor.execute(new SaveAccountDataRequestModel("none")); assertEquals("Account not found.", presenter.failure);
        dao.save(new Account("ada", 10)); storage.exception = new AccountPersistenceException("disk full", new RuntimeException());
        interactor.execute(new SaveAccountDataRequestModel("ada")); assertEquals("Failed to save account data: disk full", presenter.failure); assertNull(presenter.success);
    }
}
