package com.marketmaker.use_case.view_portfolio_summary;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.search_ticker.TickerDataAccessInterface;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewPortfolioSummaryInteractorTest {

    private static class FakePresenter implements ViewPortfolioSummaryOutputBoundary {
        ViewPortfolioSummaryResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(ViewPortfolioSummaryResponseModel response) {
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
    void computesCashEquityAndPnL() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("wayne", 90_000.0);
        account.addPosition(new Position("AAPL", 10, 200.0));
        accountDAO.save(account);
        FakePresenter presenter = new FakePresenter();
        ViewPortfolioSummaryInteractor interactor =
                new ViewPortfolioSummaryInteractor(accountDAO, new FakeQuoteDataAccess(), presenter);

        ViewPortfolioSummaryRequestModel req = new ViewPortfolioSummaryRequestModel("wayne");
        assertEquals("wayne", req.getAccountId());

        interactor.execute(req);

        ViewPortfolioSummaryResponseModel response = presenter.successResponse;
        assertEquals(90_000.0, response.getCash());
        assertEquals(90_000.0, response.getBuyingPower());
        assertEquals(92_500.0, response.getTotalEquity());
        assertEquals(500.0, response.getDailyPnL());
    }

    @Test
    void fallsBackToAveragePriceWhenQuoteIsNull() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("wayne", 90_000.0);
        account.addPosition(new Position("AAPL", 10, 200.0));
        accountDAO.save(account);
        FakePresenter presenter = new FakePresenter();
        ViewPortfolioSummaryInteractor interactor =
                new ViewPortfolioSummaryInteractor(accountDAO, ticker -> null, presenter);

        interactor.execute(new ViewPortfolioSummaryRequestModel("wayne"));

        ViewPortfolioSummaryResponseModel response = presenter.successResponse;
        assertEquals(92_000.0, response.getTotalEquity());
        assertEquals(0.0, response.getDailyPnL());
    }

    @Test
    void reportsFailureWhenAccountMissing() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        FakePresenter presenter = new FakePresenter();
        ViewPortfolioSummaryInteractor interactor =
                new ViewPortfolioSummaryInteractor(accountDAO, new FakeQuoteDataAccess(), presenter);

        interactor.execute(new ViewPortfolioSummaryRequestModel("ghost"));

        assertTrue(presenter.failureMessage.contains("Account not found"));
    }
}
