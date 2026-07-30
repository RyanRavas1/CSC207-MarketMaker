package com.marketmaker.use_case.add_to_watchlist;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.use_case.receive_live_quotes.LiveQuoteDataAccessInterface;
import com.marketmaker.use_case.receive_live_quotes.QuoteUpdateListener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddToWatchlistInteractorTest {

    private static class FakePresenter implements AddToWatchlistOutputBoundary {
        AddToWatchlistResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(AddToWatchlistResponseModel response) {
            this.successResponse = response;
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.failureMessage = errorMessage;
        }
    }

    private static class FakeLiveQuoteDataAccess implements LiveQuoteDataAccessInterface {
        String subscribedTicker;

        @Override
        public void subscribe(String ticker, QuoteUpdateListener listener) {
            this.subscribedTicker = ticker;
        }

        @Override
        public void unsubscribe(String ticker) { }
    }

    @Test
    void addsTickerAndSubscribesToLiveQuotes() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        accountDAO.save(new Account("wayne", 100_000.0));
        FakeLiveQuoteDataAccess liveQuoteDataAccess = new FakeLiveQuoteDataAccess();
        FakePresenter presenter = new FakePresenter();
        AddToWatchlistInteractor interactor = new AddToWatchlistInteractor(
                accountDAO, liveQuoteDataAccess, quote -> { }, presenter);

        interactor.execute(new AddToWatchlistRequestModel("wayne", "aapl"));

        assertNull(presenter.failureMessage);
        assertEquals("AAPL", presenter.successResponse.getTicker());
        assertTrue(presenter.successResponse.getWatchlistTickers().contains("AAPL"));
        assertEquals("AAPL", liveQuoteDataAccess.subscribedTicker);
    }

    @Test
    void rejectsDuplicateTicker() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("wayne", 100_000.0);
        account.getWatchlist().add("AAPL");
        accountDAO.save(account);
        FakePresenter presenter = new FakePresenter();
        AddToWatchlistInteractor interactor = new AddToWatchlistInteractor(
                accountDAO, new FakeLiveQuoteDataAccess(), quote -> { }, presenter);

        interactor.execute(new AddToWatchlistRequestModel("wayne", "AAPL"));

        assertNull(presenter.successResponse);
        assertTrue(presenter.failureMessage.contains("already on watchlist"));
    }

    @Test
    void reportsFailureWhenAccountMissing() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        FakePresenter presenter = new FakePresenter();
        AddToWatchlistInteractor interactor = new AddToWatchlistInteractor(
                accountDAO, new FakeLiveQuoteDataAccess(), quote -> { }, presenter);

        interactor.execute(new AddToWatchlistRequestModel("ghost", "AAPL"));

        assertTrue(presenter.failureMessage.contains("Account not found"));
    }
}
