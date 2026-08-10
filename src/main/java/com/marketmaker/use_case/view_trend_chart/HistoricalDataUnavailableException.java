package com.marketmaker.use_case.view_trend_chart;

/**
 * Thrown when price history could not be had, and the reason is worth telling the user.
 *
 * <p>An empty list already means "this ticker has no bars". This is for the cases that are
 * not about the ticker at all - a spent daily quota, a provider that would not answer - where
 * showing an empty chart invites the user to keep clicking at a wall.
 */
public class HistoricalDataUnavailableException extends RuntimeException {
    public HistoricalDataUnavailableException(String message) {
        super(message);
    }
}
