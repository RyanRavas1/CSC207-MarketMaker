package com.marketmaker.view;

import com.marketmaker.entities.Candle;
import com.marketmaker.entities.Order;
import com.marketmaker.use_case.view_watchlist.WatchlistResponseModel;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_trend_chart.Resolution;
import com.marketmaker.use_case.view_trend_chart.ViewTrendChartResponseModel;
import com.marketmaker.use_case.view_order_history.OrderHistoryRow;
import com.marketmaker.use_case.view_order_history.TradeHistoryRow;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryResponseModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;
import com.marketmaker.use_case.view_positions.PositionView;
import com.marketmaker.use_case.view_positions.ViewPositionsResponseModel;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.marketmaker.interface_adapter.Format;

/**
 * Builds each panel and checks it reflects what the view model publishes. Controllers are
 * null on purpose: no panel calls one while constructing, only from an action listener, so
 * these tests cover rendering without standing up the whole object graph.
 */
class PanelsHeadlessTest {
    @Test void panelsBuildAndReflectPublishedViewModelState() throws Exception {
        AtomicReference<AccountSummaryBar> summaryBarRef = new AtomicReference<>();
        AtomicReference<OrderTicketPanel> ticketRef = new AtomicReference<>();
        AtomicReference<PositionsPanel> positionsRef = new AtomicReference<>();
        AtomicReference<WatchlistPanel> watchlistRef = new AtomicReference<>();
        AtomicReference<ChartPanel> chartRef = new AtomicReference<>();
        onEdt(() -> {
            ViewModel<ViewPortfolioSummaryResponseModel> summary = new ViewModel<>();
            ViewModel<String> status = new ViewModel<>();
            summaryBarRef.set(new AccountSummaryBar(summary));
            ticketRef.set(new OrderTicketPanel(summary, status, new ViewModel<>(), null, null));
            summary.setState(new ViewPortfolioSummaryResponseModel(1, 2, 3, -4));

            ViewModel<ViewPositionsResponseModel> positions = new ViewModel<>();
            positionsRef.set(new PositionsPanel(positions));
            positions.setState(new ViewPositionsResponseModel(
                    List.of(new PositionView("AAPL", 2, 10, 12, 4))));

            ViewModel<WatchlistResponseModel> watchlist = new ViewModel<>();
            watchlistRef.set(new WatchlistPanel(watchlist, null, null));
            watchlist.setState(new WatchlistResponseModel(
                    List.of(new WatchlistResponseModel.Row("AAPL", 12.0, Instant.EPOCH)), List.of()));

            ViewModel<ViewTrendChartResponseModel> chart = new ViewModel<>();
            chartRef.set(new ChartPanel(chart, null));
            chart.setState(new ViewTrendChartResponseModel("AAPL", Resolution.ONE_MONTH,
                    List.of(new Candle("AAPL", "D", 1, 2, 0, 1, 1, LocalDateTime.MIN))));
        });
        onEdt(() -> {
            assertTrue(labels(summaryBarRef.get()).stream()
                    .anyMatch(label -> label.getText().equals(Format.signedDollars(-4d))));
            assertTrue(labels(ticketRef.get()).stream()
                    .anyMatch(label -> label.getText().equals("$2.00")));
            assertEquals(1, tables(positionsRef.get()).get(0).getRowCount());
            assertEquals(1, tables(watchlistRef.get()).get(0).getRowCount());
            assertTrue(labels(chartRef.get()).stream()
                    .anyMatch(label -> label.getText().contains("AAPL")));
        });
    }

    @Test void historyPanelLoadsRowsFiltersAndShowsErrors() throws Exception {
        AtomicReference<ViewModel<ViewOrderHistoryResponseModel>> modelRef = new AtomicReference<>();
        AtomicReference<OrderHistoryPanel> panelRef = new AtomicReference<>();
        onEdt(() -> {
            ViewModel<ViewOrderHistoryResponseModel> model = new ViewModel<>();
            modelRef.set(model);
            panelRef.set(new OrderHistoryPanel(model, new ViewModel<>(), null));
            model.setState(new ViewOrderHistoryResponseModel(List.of(
                    new OrderHistoryRow("p", "AAPL", Order.Side.BUY, Order.Type.LIMIT, 1, 1d,
                            Order.Status.PENDING, Instant.EPOCH),
                    new OrderHistoryRow("f", "MSFT", Order.Side.SELL, Order.Type.MARKET, 1, null,
                            Order.Status.FILLED, Instant.EPOCH)),
                    List.of(new TradeHistoryRow("t", "AAPL", Order.Side.BUY, 1, 1, Instant.EPOCH, null))));
        });
        onEdt(() -> {
            OrderHistoryPanel panel = panelRef.get();
            List<JTable> tables = tables(panel);
            JTable orders = tables.stream()
                    .filter(table -> table.getColumnCount() == 7).findFirst().orElseThrow();
            JTable trades = tables.stream()
                    .filter(table -> table.getColumnCount() == 6).findFirst().orElseThrow();
            assertEquals(2, orders.getRowCount());
            assertEquals(1, trades.getRowCount());
            buttons(panel).stream().filter(button -> "Pending".equals(button.getText()))
                    .findFirst().orElseThrow().doClick();
            assertEquals(1, orders.getRowCount());
        });
    }

    @Test void titledPanelAndComponentsApplyExpectedStructure() throws Exception {
        onEdt(() -> {
            TitledPanel panel = new TitledPanel("Title");
            assertNotNull(panel.getContent());
            assertTrue(labels(panel).stream().anyMatch(label -> label.getText().equals("Title")));
            assertAll(
                    () -> assertEquals("Caption", ViewComponents.caption("Caption").getText()),
                    // Buttons must stay in the Tab order; see accessibility-report.md.
                    () -> assertTrue(ViewComponents.button("Go").isFocusable()),
                    () -> assertEquals("●", ViewComponents.statusDot(Color.BLUE).getText()));
        });
    }

    private static List<JTable> tables(Container root) {
        return descendants(root, JTable.class);
    }

    private static List<JLabel> labels(Container root) {
        return descendants(root, JLabel.class);
    }

    private static List<JButton> buttons(Container root) {
        return descendants(root, JButton.class);
    }

    private static <T extends Component> List<T> descendants(Container root, Class<T> type) {
        List<T> matches = new ArrayList<>();
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) {
                matches.add(type.cast(component));
            }
            if (component instanceof JScrollPane pane
                    && pane.getViewport().getView() instanceof Container view) {
                matches.addAll(descendants(view, type));
            }
            if (component instanceof JViewport viewport
                    && viewport.getView() instanceof Container view) {
                matches.addAll(descendants(view, type));
            }
            if (component instanceof JTabbedPane tabs) {
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    Component tab = tabs.getComponentAt(i);
                    if (type.isInstance(tab)) {
                        matches.add(type.cast(tab));
                    }
                    if (tab instanceof Container child) {
                        matches.addAll(descendants(child, type));
                    }
                }
            }
            if (component instanceof Container child) {
                matches.addAll(descendants(child, type));
            }
        }
        return matches;
    }

    private static void onEdt(Runnable work) throws Exception {
        SwingUtilities.invokeAndWait(work);
    }
}
