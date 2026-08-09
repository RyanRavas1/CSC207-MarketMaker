package com.marketmaker.use_case.remove_from_watchlist;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.use_case.receive_live_quotes.LiveQuoteDataAccessInterface;
import com.marketmaker.use_case.receive_live_quotes.QuoteUpdateListener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoveFromWatchlistInteractorTest {

    private static final class FakePresenter implements RemoveFromWatchlistOutputBoundary {
        RemoveFromWatchlistResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(RemoveFromWatchlistResponseModel response) {
            this.successResponse = response;
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.failureMessage = errorMessage;
        }
    }

    private static final class FakeLiveQuoteDataAccess implements LiveQuoteDataAccessInterface {
        String unsubscribedTicker;

        @Override
        public void subscribe(String ticker, QuoteUpdateListener listener) { }

        @Override
        public void unsubscribe(String ticker) {
            this.unsubscribedTicker = ticker;
        }
    }

    @Test
    void removesTickerAndUnsubscribes() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("wayne", 100_000.0);
        account.getWatchlist().add("AAPL");
        accountDAO.save(account);
        FakeLiveQuoteDataAccess liveQuoteDataAccess = new FakeLiveQuoteDataAccess();
        FakePresenter presenter = new FakePresenter();
        RemoveFromWatchlistInteractor interactor =
                new RemoveFromWatchlistInteractor(accountDAO, liveQuoteDataAccess, presenter);

        interactor.execute(new RemoveFromWatchlistRequestModel("wayne", "aapl"));

        assertNull(presenter.failureMessage);
        assertFalse(account.getWatchlist().contains("AAPL"));
        assertEquals("AAPL", liveQuoteDataAccess.unsubscribedTicker);
    }

    @Test
    void reportsFailureWhenTickerNotOnWatchlist() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        accountDAO.save(new Account("wayne", 100_000.0));
        FakePresenter presenter = new FakePresenter();
        RemoveFromWatchlistInteractor interactor =
                new RemoveFromWatchlistInteractor(accountDAO, new FakeLiveQuoteDataAccess(), presenter);

        interactor.execute(new RemoveFromWatchlistRequestModel("wayne", "AAPL"));

        assertNull(presenter.successResponse);
        assertTrue(presenter.failureMessage.contains("not on watchlist"));
    }
}
