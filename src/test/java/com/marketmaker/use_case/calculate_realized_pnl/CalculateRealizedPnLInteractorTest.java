package com.marketmaker.use_case.calculate_realized_pnl;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculateRealizedPnLInteractorTest {

    private static final class FakePresenter implements CalculateRealizedPnLOutputBoundary {
        CalculateRealizedPnLResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(CalculateRealizedPnLResponseModel response) {
            this.successResponse = response;
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.failureMessage = errorMessage;
        }
    }

    @Test
    void computesGainOnPartialSale() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("wayne", 100_000.0);
        account.addPosition(new Position("AAPL", 10, 200.0));
        accountDAO.save(account);
        FakePresenter presenter = new FakePresenter();
        CalculateRealizedPnLInteractor interactor = new CalculateRealizedPnLInteractor(accountDAO, presenter);

        CalculateRealizedPnLRequestModel req = new CalculateRealizedPnLRequestModel("wayne", "AAPL", 4, 250.0);
        assertEquals("wayne", req.getAccountId());
        assertEquals("AAPL", req.getTicker());
        assertEquals(4, req.getQuantitySold());
        assertEquals(250.0, req.getSalePrice());

        interactor.execute(req);

        assertNull(presenter.failureMessage);
        assertEquals("AAPL", presenter.successResponse.getTicker());
        assertEquals(4, presenter.successResponse.getQuantitySold());
        assertEquals(200.0, presenter.successResponse.getRealizedPnL());
    }

    @Test
    void rejectsSellingMoreSharesThanHeld() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("wayne", 100_000.0);
        account.addPosition(new Position("AAPL", 10, 200.0));
        accountDAO.save(account);
        FakePresenter presenter = new FakePresenter();
        CalculateRealizedPnLInteractor interactor = new CalculateRealizedPnLInteractor(accountDAO, presenter);

        interactor.execute(new CalculateRealizedPnLRequestModel("wayne", "AAPL", 20, 250.0));

        assertTrue(presenter.failureMessage.contains("Cannot sell more shares"));
    }

    @Test
    void reportsFailureWhenNoPositionExists() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        accountDAO.save(new Account("wayne", 100_000.0));
        FakePresenter presenter = new FakePresenter();
        CalculateRealizedPnLInteractor interactor = new CalculateRealizedPnLInteractor(accountDAO, presenter);

        interactor.execute(new CalculateRealizedPnLRequestModel("wayne", "AAPL", 1, 250.0));

        assertTrue(presenter.failureMessage.contains("No position"));
    }

    @Test
    void rejectsMissingAccountAndNonPositiveQuantity() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO();
        FakePresenter missing = new FakePresenter();
        new CalculateRealizedPnLInteractor(dao, missing).execute(new CalculateRealizedPnLRequestModel("ghost", "AAPL", 1, 1));
        assertEquals("Account not found.", missing.failureMessage);

        Account account = new Account("wayne", 0); account.addPosition(new Position("AAPL", 1, 1)); dao.save(account);
        FakePresenter invalid = new FakePresenter();
        new CalculateRealizedPnLInteractor(dao, invalid).execute(new CalculateRealizedPnLRequestModel("wayne", "AAPL", 0, 1));
        assertEquals("Quantity sold must be positive.", invalid.failureMessage);
    }
}
