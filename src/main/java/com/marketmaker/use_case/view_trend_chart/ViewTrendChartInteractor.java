package com.marketmaker.use_case.view_trend_chart;

import java.util.List;

import com.marketmaker.entities.Candle;

/**
 * Displays historical price candles for a selected ticker. Also backs Switch Chart Interval:
 * the caller re-invokes execute() with a different resolution to reload the same chart.
 */
public class ViewTrendChartInteractor implements ViewTrendChartInputBoundary {
    private final HistoricalDataAccessInterface dataAccess;
    private final ViewTrendChartOutputBoundary presenter;

    public ViewTrendChartInteractor(HistoricalDataAccessInterface dataAccess,
                                    ViewTrendChartOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(ViewTrendChartRequestModel request) {
        String ticker = request.getTicker();
        if (ticker == null || ticker.isBlank()) {
            presenter.presentFailure("Select a ticker to chart.");
            return;
        }

        List<Candle> candles;
        try {
            candles = dataAccess.fetchCandles(ticker.toUpperCase(), request.getResolution());
        }
        catch (HistoricalDataUnavailableException exception) {
            // The provider explained itself, so pass that on rather than blaming the ticker.
            presenter.presentFailure(exception.getMessage());
            return;
        }

        if (candles == null || candles.isEmpty()) {
            presenter.presentFailure("No price history for " + ticker.toUpperCase()
                    + " - check the symbol.");
            return;
        }

        presenter.presentSuccess(new ViewTrendChartResponseModel(
                ticker.toUpperCase(), request.getResolution(), candles));
    }
}
