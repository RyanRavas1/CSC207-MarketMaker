package com.marketmaker.use_case.create_account;

import com.marketmaker.data_access.InMemoryAccountDAO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateAccountInteractorTest {

    private static class FakePresenter implements CreateAccountOutputBoundary {
        CreateAccountResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(CreateAccountResponseModel response) {
            this.successResponse = response;
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.failureMessage = errorMessage;
        }
    }

    @Test
    void createsNewAccountWithStartingBalance() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        FakePresenter presenter = new FakePresenter();
        CreateAccountInteractor interactor = new CreateAccountInteractor(accountDAO, presenter);

        interactor.execute(new CreateAccountRequestModel("wayne"));

        assertNull(presenter.failureMessage);
        assertEquals("wayne", presenter.successResponse.getAccountId());
        assertEquals(100_000.0, presenter.successResponse.getStartingBalance());
        assertEquals(100_000.0, accountDAO.get("wayne").getUserBalance());
    }

    @Test
    void rejectsCreatingDuplicateAccount() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        FakePresenter presenter = new FakePresenter();
        CreateAccountInteractor interactor = new CreateAccountInteractor(accountDAO, presenter);
        interactor.execute(new CreateAccountRequestModel("wayne"));

        presenter.successResponse = null;
        interactor.execute(new CreateAccountRequestModel("wayne"));

        assertTrue(presenter.failureMessage.contains("already exists"));
        assertNull(presenter.successResponse);
    }
}
