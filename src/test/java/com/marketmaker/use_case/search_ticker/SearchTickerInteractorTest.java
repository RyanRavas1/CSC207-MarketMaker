package com.marketmaker.use_case.search_ticker;

import com.marketmaker.entities.Quote;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchTickerInteractorTest {

    private static final class FakePresenter implements SearchTickerOutputBoundary {
        SearchTickerResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(SearchTickerResponseModel response) {
            this.successResponse = response;
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.failureMessage = errorMessage;
        }
    }

    private static final class FakeTickerDataAccess implements TickerDataAccessInterface {
        @Override
        public Quote fetchQuote(String ticker) {
            if (!ticker.equals("AAPL")) {
                return null;
            }
            return new Quote("AAPL", 232.50, Instant.EPOCH);
        }
    }

    @Test
    void returnsQuoteForKnownTicker() {
        FakePresenter presenter = new FakePresenter();
        SearchTickerInteractor interactor = new SearchTickerInteractor(new FakeTickerDataAccess(), presenter);

        interactor.execute(new SearchTickerRequestModel("aapl"));

        assertNull(presenter.failureMessage);
        assertEquals("AAPL", presenter.successResponse.getTicker());
        assertEquals(232.50, presenter.successResponse.getPrice());
        assertEquals(Instant.EPOCH, presenter.successResponse.getTimestamp());
    }

    @Test
    void reportsFailureForUnknownTicker() {
        FakePresenter presenter = new FakePresenter();
        SearchTickerInteractor interactor = new SearchTickerInteractor(new FakeTickerDataAccess(), presenter);

        interactor.execute(new SearchTickerRequestModel("ZZZZ"));

        assertNull(presenter.successResponse);
        assertTrue(presenter.failureMessage.contains("No quote found"));
    }

    @Test
    void reportsFailureForNullOrBlankTicker() {
        FakePresenter presenter = new FakePresenter();
        SearchTickerInteractor interactor = new SearchTickerInteractor(new FakeTickerDataAccess(), presenter);

        SearchTickerRequestModel blankRequest = new SearchTickerRequestModel("  ");
        assertEquals("  ", blankRequest.getTicker());
        interactor.execute(blankRequest);
        assertTrue(presenter.failureMessage.contains("Enter a ticker"));

        presenter.failureMessage = null;
        interactor.execute(new SearchTickerRequestModel(null));
        assertTrue(presenter.failureMessage.contains("Enter a ticker"));
    }
}
