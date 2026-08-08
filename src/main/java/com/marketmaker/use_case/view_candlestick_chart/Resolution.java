package com.marketmaker.use_case.view_candlestick_chart;

/**
 * How much price history the chart shows.
 *
 * <p>These are spans rather than bar sizes because the bars themselves are daily: Alpha
 * Vantage's intraday endpoint is a paid one, so a free key can only see one price per day.
 * Trading days, not calendar days — the market is shut at weekends, so a week is five bars.
 */
public enum Resolution {
    ONE_WEEK(5),
    ONE_MONTH(22);

    private final int tradingDays;

    Resolution(int tradingDays) {
        this.tradingDays = tradingDays;
    }

    public int getTradingDays() {
        return tradingDays;
    }
}
