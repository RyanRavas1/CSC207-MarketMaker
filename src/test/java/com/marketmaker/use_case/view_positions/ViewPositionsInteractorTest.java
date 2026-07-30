package com.marketmaker.use_case.view_positions;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.search_ticker.TickerDataAccessInterface;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ViewPositionsInteractorTest {

    private static class FakePresenter implements ViewPositionsOutputBoundary {
        ViewPositionsResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(ViewPositionsResponseModel response) {
            this.successResponse = response;
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.failureMessage = errorMessage;
        }
    }

    private static class FakeQuoteDataAccess implements TickerDataAccessInterface {
        @Override
        public Quote fetchQuote(String ticker) {
            return new Quote(ticker, 250.0, Instant.EPOCH);
        }
    }

    @Test
    void computesUnrealizedPnLForEachHolding() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("wayne", 100_000.0);
        account.addPosition(new Position("AAPL", 10, 200.0));
        accountDAO.save(account);
        FakePresenter presenter = new FakePresenter();
        ViewPositionsInteractor interactor =
                new ViewPositionsInteractor(accountDAO, new FakeQuoteDataAccess(), presenter);

        interactor.execute(new ViewPositionsRequestModel("wayne"));

        PositionView view = presenter.successResponse.getPositions().get(0);
        assertEquals("AAPL", view.getTicker());
        assertEquals(250.0, view.getCurrentPrice());
        assertEquals(500.0, view.getUnrealizedPnL());
    }

    @Test
    void reportsFailureWhenAccountMissing() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        FakePresenter presenter = new FakePresenter();
        ViewPositionsInteractor interactor =
                new ViewPositionsInteractor(accountDAO, new FakeQuoteDataAccess(), presenter);

        interactor.execute(new ViewPositionsRequestModel("ghost"));

        assertTrue(presenter.failureMessage.contains("Account not found"));
    }
}
