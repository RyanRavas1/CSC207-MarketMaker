package com.marketmaker;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.marketmaker.entities.Candle;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.view_candlestick_chart.Resolution;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartResponseModel;
import com.marketmaker.use_case.view_order_history.OrderHistoryRow;
import com.marketmaker.use_case.view_order_history.TradeHistoryRow;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryResponseModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;
import com.marketmaker.use_case.view_positions.PositionView;
import com.marketmaker.use_case.view_positions.ViewPositionsResponseModel;

/**
 * Sample responses so the dashboard has something to draw before the interactors are
 * wired to a real account and price feed.
 *
 * <p>These are the same response models the interactors produce, pushed through the
 * same presenters, so swapping in the real thing is a change in {@link Main} only —
 * no panel changes. Delete this class once that wiring exists.
 */
final class DemoData {

    private static final Instant OPEN_BELL = Instant.now().truncatedTo(ChronoUnit.HOURS);

    private DemoData() {
    }

    static ViewPortfolioSummaryResponseModel summary() {
        return new ViewPortfolioSummaryResponseModel(12480.55, 24961.10, 28714.90, 342.18);
    }

    static ViewPositionsResponseModel positions() {
        return new ViewPositionsResponseModel(List.of(
                new PositionView("AAPL", 30, 224.10, 229.35, 157.50),
                new PositionView("NVDA", 15, 118.20, 121.44, 48.60),
                new PositionView("TSLA", 10, 250.00, 242.15, -78.50),
                new PositionView("MSFT", 8, 430.10, 433.28, 25.44)));
    }

    static ViewOrderHistoryResponseModel orderHistory() {
        List<OrderHistoryRow> orders = List.of(
                new OrderHistoryRow("o-1", "AAPL", Order.Side.BUY, Order.Type.MARKET,
                        30, null, Order.Status.FILLED, OPEN_BELL.plusSeconds(64)),
                new OrderHistoryRow("o-2", "NVDA", Order.Side.BUY, Order.Type.LIMIT,
                        15, 118.00, Order.Status.FILLED, OPEN_BELL.plusSeconds(912)),
                new OrderHistoryRow("o-3", "TSLA", Order.Side.SELL, Order.Type.LIMIT,
                        10, 250.00, Order.Status.PENDING, OPEN_BELL.plusSeconds(1957)),
                new OrderHistoryRow("o-4", "SPY", Order.Side.BUY, Order.Type.MARKET,
                        20, null, Order.Status.CANCELED, OPEN_BELL.plusSeconds(2931)),
                new OrderHistoryRow("o-5", "MSFT", Order.Side.SELL, Order.Type.STOP_LOSS,
                        8, 428.00, Order.Status.PENDING, OPEN_BELL.plusSeconds(3402)));

        List<TradeHistoryRow> trades = List.of(
                new TradeHistoryRow("t-1", "AAPL", Order.Side.BUY, 30, 224.10,
                        OPEN_BELL.plusSeconds(64), null),
                new TradeHistoryRow("t-2", "NVDA", Order.Side.BUY, 15, 118.20,
                        OPEN_BELL.plusSeconds(912), null),
                new TradeHistoryRow("t-3", "AMD", Order.Side.SELL, 25, 168.55,
                        OPEN_BELL.plusSeconds(2410), 152.30));

        return new ViewOrderHistoryResponseModel(orders, trades);
    }

    static ViewCandlestickChartResponseModel chart() {
        List<Candle> candles = List.of(
                bar(228.90, 229.20, 228.70, 229.05, 1_240_000, 0),
                bar(229.05, 229.40, 228.95, 229.30, 1_180_000, 60),
                bar(229.30, 229.55, 229.10, 229.20, 990_000, 120),
                bar(229.20, 229.35, 228.85, 228.95, 1_070_000, 180),
                bar(228.95, 229.45, 228.90, 229.40, 1_310_000, 240),
                bar(229.40, 229.60, 229.25, 229.35, 1_420_000, 300));
        return new ViewCandlestickChartResponseModel("AAPL", Resolution.ONE_MINUTE, candles);
    }

    static List<Quote> watchlist() {
        Instant now = Instant.now();
        return List.of(
                new Quote("AAPL", 229.35, now),
                new Quote("NVDA", 121.44, now),
                new Quote("TSLA", 242.15, now),
                new Quote("MSFT", 433.28, now),
                new Quote("AMZN", 201.30, now),
                new Quote("SPY", 559.12, now),
                new Quote("AMD", 168.55, now),
                new Quote("META", 597.40, now),
                new Quote("GOOGL", 178.66, now));
    }

    private static Candle bar(double open, double high, double low, double close,
                              double volume, int secondsIn) {
        return new Candle("AAPL", "1", open, high, low, close, volume,
                LocalDateTime.ofInstant(OPEN_BELL.plusSeconds(secondsIn), ZoneOffset.UTC));
    }
}
