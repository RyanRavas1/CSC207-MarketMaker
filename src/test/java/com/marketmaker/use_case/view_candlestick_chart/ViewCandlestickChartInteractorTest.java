package com.marketmaker.use_case.view_candlestick_chart;

import com.marketmaker.entities.Candle;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ViewCandlestickChartInteractorTest {

    private static final class FakePresenter implements ViewCandlestickChartOutputBoundary {
        ViewCandlestickChartResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(ViewCandlestickChartResponseModel response) {
            this.successResponse = response;
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.failureMessage = errorMessage;
        }
    }

    private static final class FakeHistoricalDataAccess implements HistoricalDataAccessInterface {
        Resolution requestedResolution;

        @Override
        public List<Candle> fetchCandles(String ticker, Resolution resolution) {
            this.requestedResolution = resolution;
            if (!ticker.equals("AAPL")) {
                return List.of();
            }
            return List.of(new Candle("AAPL", "D", 230.0, 235.0, 228.0, 232.5, 1000.0,
                    LocalDateTime.of(2026, 8, 6, 0, 0)));
        }
    }

    @Test
    void returnsCandlesForKnownTicker() {
        FakeHistoricalDataAccess dataAccess = new FakeHistoricalDataAccess();
        FakePresenter presenter = new FakePresenter();
        ViewCandlestickChartInteractor interactor = new ViewCandlestickChartInteractor(dataAccess, presenter);

        interactor.execute(new ViewCandlestickChartRequestModel("aapl", Resolution.ONE_MONTH));

        assertNull(presenter.failureMessage);
        assertEquals(1, presenter.successResponse.getCandles().size());
        assertEquals(Resolution.ONE_MONTH, presenter.successResponse.getResolution());
    }

    @Test
    void switchingIntervalReReloadsWithNewResolution() {
        FakeHistoricalDataAccess dataAccess = new FakeHistoricalDataAccess();
        ViewCandlestickChartInteractor interactor =
                new ViewCandlestickChartInteractor(dataAccess, new FakePresenter());

        interactor.execute(new ViewCandlestickChartRequestModel("AAPL", Resolution.ONE_WEEK));
        assertEquals(Resolution.ONE_WEEK, dataAccess.requestedResolution);

        interactor.execute(new ViewCandlestickChartRequestModel("AAPL", Resolution.ONE_MONTH));
        assertEquals(Resolution.ONE_MONTH, dataAccess.requestedResolution);
    }

    @Test
    void reportsFailureWhenNoDataAvailable() {
        FakePresenter presenter = new FakePresenter();
        ViewCandlestickChartInteractor interactor =
                new ViewCandlestickChartInteractor(new FakeHistoricalDataAccess(), presenter);

        interactor.execute(new ViewCandlestickChartRequestModel("ZZZZ", Resolution.ONE_MONTH));

        // Names the symbol and what to do about it, rather than just reporting emptiness.
        assertTrue(presenter.failureMessage.contains("ZZZZ"));
        assertTrue(presenter.failureMessage.contains("check the symbol"));
    }

    @Test
    void passesOnTheProvidersOwnReasonWhenItGivesOne() {
        HistoricalDataAccessInterface refusing = (ticker, resolution) -> {
            throw new HistoricalDataUnavailableException("Daily price-history limit reached.");
        };
        FakePresenter presenter = new FakePresenter();

        new ViewCandlestickChartInteractor(refusing, presenter)
                .execute(new ViewCandlestickChartRequestModel("AAPL", Resolution.ONE_MONTH));

        // A spent quota is not the ticker's fault, so the message must not blame the symbol.
        assertEquals("Daily price-history limit reached.", presenter.failureMessage);
    }
}
