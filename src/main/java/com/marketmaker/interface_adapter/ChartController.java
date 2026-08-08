package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.view_candlestick_chart.Resolution;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartInputBoundary;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartRequestModel;

/**
 * Loads the chart, and reloads it at a new interval when the user picks one.
 *
 * <p>Remembers the ticker and interval currently on screen so a refresh can repeat the same
 * request without the view having to hand them back.
 */
public class ChartController {
    private final ViewCandlestickChartInputBoundary interactor;

    private String ticker;
    private Resolution resolution;

    public ChartController(ViewCandlestickChartInputBoundary interactor,
                           String ticker, Resolution resolution) {
        this.interactor = interactor;
        this.ticker = ticker;
        this.resolution = resolution;
    }

    public void show(String ticker) {
        this.ticker = ticker == null ? this.ticker : ticker.trim().toUpperCase();
        reload();
    }

    public void showInterval(Resolution resolution) {
        this.resolution = resolution;
        reload();
    }

    public void reload() {
        interactor.execute(new ViewCandlestickChartRequestModel(ticker, resolution));
    }

    public Resolution getResolution() { return resolution; }
}
