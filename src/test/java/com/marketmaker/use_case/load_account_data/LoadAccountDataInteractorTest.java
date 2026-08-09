package com.marketmaker.use_case.load_account_data;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadAccountDataInteractorTest {

    private static class FakePresenter implements LoadAccountDataOutputBoundary {
        LoadAccountDataResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(LoadAccountDataResponseModel response) {
            this.successResponse = response;
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.failureMessage = errorMessage;
        }
    }

    private static class FakeFileDataAccess implements LoadAccountDataAccessInterface {
        Account savedAccount;

        @Override
        public Account load(String accountId) {
            return savedAccount != null && savedAccount.getUserName().equals(accountId) ? savedAccount : null;
        }
    }

    @Test
    void restoresAccountIntoInMemoryStore() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        FakeFileDataAccess fileDataAccess = new FakeFileDataAccess();
        fileDataAccess.savedAccount = new Account("wayne", 87_500.0);
        FakePresenter presenter = new FakePresenter();
        LoadAccountDataInteractor interactor =
                new LoadAccountDataInteractor(accountDAO, fileDataAccess, presenter);

        LoadAccountDataRequestModel req = new LoadAccountDataRequestModel("wayne");
        assertEquals("wayne", req.getAccountId());

        interactor.execute(req);

        assertNull(presenter.failureMessage);
        assertEquals("wayne", presenter.successResponse.getAccountId());
        assertEquals(87_500.0, presenter.successResponse.getUserBalance());
        assertEquals(87_500.0, accountDAO.get("wayne").getUserBalance());
    }

    @Test
    void reportsFailureWhenNoSavedDataExists() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        FakePresenter presenter = new FakePresenter();
        LoadAccountDataInteractor interactor =
                new LoadAccountDataInteractor(accountDAO, new FakeFileDataAccess(), presenter);

        interactor.execute(new LoadAccountDataRequestModel("ghost"));

        assertTrue(presenter.failureMessage.contains("No saved data found"));
    }

    @Test
    void handlesAccountPersistenceException() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        FakePresenter presenter = new FakePresenter();
        LoadAccountDataInteractor interactor = new LoadAccountDataInteractor(
                accountDAO,
                accountId -> { throw new com.marketmaker.data_access.exceptions.
                        AccountPersistenceException("Corrupted JSON", new RuntimeException()); },
                presenter);

        interactor.execute(new LoadAccountDataRequestModel("corrupt"));

        assertTrue(presenter.failureMessage.contains("Failed to load account data: Corrupted JSON"));
    }
}
