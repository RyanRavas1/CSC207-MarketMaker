package com.marketmaker.view;

import com.marketmaker.entities.Candle;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Quote;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_candlestick_chart.Resolution;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartResponseModel;
import com.marketmaker.use_case.view_order_history.OrderHistoryRow;
import com.marketmaker.use_case.view_order_history.TradeHistoryRow;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryResponseModel;
import com.marketmaker.use_case.order_history.OrderHistoryEntry;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;
import com.marketmaker.use_case.view_positions.PositionView;
import com.marketmaker.use_case.view_positions.ViewPositionsResponseModel;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PanelsHeadlessTest {
    @Test void panelsBuildAndReflectPublishedViewModelState() throws Exception {
        java.util.concurrent.atomic.AtomicReference<AccountSummaryBar> summaryBarRef = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<OrderTicketPanel> ticketRef = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<PositionsPanel> positionsRef = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<WatchlistPanel> watchlistRef = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<ChartPanel> chartRef = new java.util.concurrent.atomic.AtomicReference<>();
        onEdt(() -> {
            ViewModel<ViewPortfolioSummaryResponseModel> summary = new ViewModel<>();
            AccountSummaryBar summaryBar = new AccountSummaryBar(summary);
            OrderTicketPanel ticket = new OrderTicketPanel(summary);
            summaryBarRef.set(summaryBar); ticketRef.set(ticket);
            summary.setState(new ViewPortfolioSummaryResponseModel(1, 2, 3, -4));

            ViewModel<ViewPositionsResponseModel> positions = new ViewModel<>(); PositionsPanel positionsPanel = new PositionsPanel(positions);
            positionsRef.set(positionsPanel);
            positions.setState(new ViewPositionsResponseModel(List.of(new PositionView("AAPL", 2, 10, 12, 4))));

            ViewModel<List<Quote>> watchlist = new ViewModel<>(); WatchlistPanel watchlistPanel = new WatchlistPanel(watchlist);
            watchlistRef.set(watchlistPanel);
            watchlist.setState(List.of(new Quote("AAPL", 12, Instant.EPOCH)));

            ViewModel<ViewCandlestickChartResponseModel> chart = new ViewModel<>(); ChartPanel chartPanel = new ChartPanel(chart);
            chartRef.set(chartPanel);
            chart.setState(new ViewCandlestickChartResponseModel("AAPL", Resolution.ONE_DAY, List.of(new Candle("AAPL", "D", 1, 2, 0, 1, 1, LocalDateTime.MIN))));
        });
        onEdt(() -> {
            assertTrue(labels(summaryBarRef.get()).stream().anyMatch(label -> label.getText().equals(Format.signedDollars(-4d))));
            assertTrue(labels(ticketRef.get()).stream().anyMatch(label -> label.getText().equals("$2.00")));
            assertEquals(1, tables(positionsRef.get()).get(0).getRowCount());
            assertEquals(1, tables(watchlistRef.get()).get(0).getRowCount());
            assertTrue(labels(chartRef.get()).stream().anyMatch(label -> label.getText().contains("AAPL")));
        });
    }

    @Test void historyPanelLoadsRowsFiltersAndShowsErrors() throws Exception {
        java.util.concurrent.atomic.AtomicReference<ViewModel<ViewOrderHistoryResponseModel>> modelRef = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<OrderHistoryPanel> panelRef = new java.util.concurrent.atomic.AtomicReference<>();
        onEdt(() -> {
            ViewModel<ViewOrderHistoryResponseModel> model = new ViewModel<>(); OrderHistoryPanel panel = new OrderHistoryPanel(model);
            modelRef.set(model); panelRef.set(panel);
            model.setState(new ViewOrderHistoryResponseModel(List.of(
                    new OrderHistoryRow("p", "AAPL", Order.Side.BUY, Order.Type.LIMIT, 1, 1d, Order.Status.PENDING, Instant.EPOCH),
                    new OrderHistoryRow("f", "MSFT", Order.Side.SELL, Order.Type.MARKET, 1, null, Order.Status.FILLED, Instant.EPOCH)),
                    List.of(new TradeHistoryRow("t", "AAPL", Order.Side.BUY, 1, 1, Instant.EPOCH, null))));
        });
        onEdt(() -> {
            OrderHistoryPanel panel = panelRef.get();
            List<JTable> tables = tables(panel);
            JTable orders = tables.stream().filter(table -> table.getColumnCount() == 7).findFirst().orElseThrow();
            JTable trades = tables.stream().filter(table -> table.getColumnCount() == 6).findFirst().orElseThrow();
            assertEquals(2, orders.getRowCount()); assertEquals(1, trades.getRowCount());
            buttons(panel).stream().filter(button -> button.getText().equals("Pending")).findFirst().orElseThrow().doClick();
            assertEquals(1, orders.getRowCount());
            modelRef.get().setError("load failed"); assertTrue(labels(panel).stream().anyMatch(label -> label.getText().equals("load failed")));
        });
    }

    @Test void titledPanelAndComponentsApplyExpectedStructure() throws Exception {
        onEdt(() -> {
            TitledPanel panel = new TitledPanel("Title"); assertNotNull(panel.getContent()); assertTrue(labels(panel).stream().anyMatch(label -> label.getText().equals("Title")));
            assertEquals("Caption", ViewComponents.caption("Caption").getText()); assertFalse(ViewComponents.button("Go").isFocusable()); assertEquals("●", ViewComponents.statusDot(Color.BLUE).getText());
        });
    }

    @Test void legacyOrderHistoryViewBuildsReadOnlyHistoryTable() throws Exception {
        onEdt(() -> {
            OrderHistoryView view = new OrderHistoryView(() -> List.of(new OrderHistoryEntry("09:30", "AAPL", "BUY", "MARKET", 2, "-", "Filled", "-")));
            JTable table = tables(view).get(0);
            assertAll(() -> assertEquals(1, table.getRowCount()), () -> assertEquals(8, table.getColumnCount()), () -> assertFalse(table.isCellEditable(0, 0)), () -> assertFalse(table.getTableHeader().getReorderingAllowed()));
        });
    }

    private static List<JTable> tables(Container root) { return descendants(root, JTable.class); }
    private static List<JLabel> labels(Container root) { return descendants(root, JLabel.class); }
    private static List<JButton> buttons(Container root) { return descendants(root, JButton.class); }
    private static <T extends Component> List<T> descendants(Container root, Class<T> type) {
        java.util.ArrayList<T> matches = new java.util.ArrayList<>();
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) matches.add(type.cast(component));
            if (component instanceof JScrollPane pane && pane.getViewport().getView() instanceof Container view) matches.addAll(descendants(view, type));
            if (component instanceof JViewport viewport && viewport.getView() instanceof Container view) matches.addAll(descendants(view, type));
            if (component instanceof JTabbedPane tabs) for (int i = 0; i < tabs.getTabCount(); i++) {
                Component tab = tabs.getComponentAt(i);
                if (type.isInstance(tab)) matches.add(type.cast(tab));
                if (tab instanceof Container child) matches.addAll(descendants(child, type));
            }
            if (component instanceof Container child) matches.addAll(descendants(child, type));
        }
        return matches;
    }
    private static void onEdt(Runnable work) throws Exception { SwingUtilities.invokeAndWait(work); }
}
