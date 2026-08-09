package com.marketmaker.use_case.view_candlestick_chart;

import com.marketmaker.entities.Candle;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewCandlestickChartInteractorTest {

    private static class FakePresenter implements ViewCandlestickChartOutputBoundary {
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

    private static class FakeHistoricalDataAccess implements HistoricalDataAccessInterface {
        Resolution requestedResolution;

        @Override
        public List<Candle> fetchCandles(String ticker, Resolution resolution) {
            this.requestedResolution = resolution;
            if (!ticker.equals("AAPL")) {
                return List.of();
            }
            return List.of(new Candle("AAPL", "D", 230.0, 235.0, 228.0, 232.5, 1000.0, LocalDateTime.MIN));
        }
    }

    @Test
    void returnsCandlesForKnownTicker() {
        FakeHistoricalDataAccess dataAccess = new FakeHistoricalDataAccess();
        FakePresenter presenter = new FakePresenter();
        ViewCandlestickChartInteractor interactor = new ViewCandlestickChartInteractor(dataAccess, presenter);

        interactor.execute(new ViewCandlestickChartRequestModel("aapl", Resolution.ONE_DAY));

        assertNull(presenter.failureMessage);
        assertEquals("AAPL", presenter.successResponse.getTicker());
        assertEquals(Resolution.ONE_DAY, presenter.successResponse.getResolution());
        assertEquals(1, presenter.successResponse.getCandles().size());
    }

    @Test
    void switchingIntervalReReloadsWithNewResolution() {
        FakeHistoricalDataAccess dataAccess = new FakeHistoricalDataAccess();
        ViewCandlestickChartInteractor interactor =
                new ViewCandlestickChartInteractor(dataAccess, new FakePresenter());

        interactor.execute(new ViewCandlestickChartRequestModel("AAPL", Resolution.ONE_MINUTE));
        assertEquals(Resolution.ONE_MINUTE, dataAccess.requestedResolution);

        interactor.execute(new ViewCandlestickChartRequestModel("AAPL", Resolution.FIVE_MINUTE));
        assertEquals(Resolution.FIVE_MINUTE, dataAccess.requestedResolution);

        interactor.execute(new ViewCandlestickChartRequestModel("AAPL", Resolution.ONE_DAY));
        assertEquals(Resolution.ONE_DAY, dataAccess.requestedResolution);
    }

    @Test
    void reportsFailureWhenNoDataAvailable() {
        FakePresenter presenter = new FakePresenter();
        ViewCandlestickChartInteractor interactor =
                new ViewCandlestickChartInteractor(new FakeHistoricalDataAccess(), presenter);

        interactor.execute(new ViewCandlestickChartRequestModel("ZZZZ", Resolution.ONE_DAY));

        assertTrue(presenter.failureMessage.contains("No historical data"));
    }

    @Test
    void rejectsNullOrBlankTickerAndNullCandleResponse() {
        FakePresenter blank = new FakePresenter();
        ViewCandlestickChartInteractor interactor = new ViewCandlestickChartInteractor((ticker, resolution) -> List.of(), blank);

        interactor.execute(new ViewCandlestickChartRequestModel(" ", Resolution.ONE_DAY));
        assertEquals("Select a ticker to chart.", blank.failureMessage);

        blank.failureMessage = null;
        interactor.execute(new ViewCandlestickChartRequestModel(null, Resolution.ONE_DAY));
        assertEquals("Select a ticker to chart.", blank.failureMessage);

        FakePresenter nullResponse = new FakePresenter();
        new ViewCandlestickChartInteractor((ticker, resolution) -> null, nullResponse)
                .execute(new ViewCandlestickChartRequestModel("AAPL", Resolution.ONE_DAY));
        assertTrue(nullResponse.failureMessage.contains("No historical data"));
    }
}
