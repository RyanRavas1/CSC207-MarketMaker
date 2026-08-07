package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marketmaker.entities.Quote;
import com.marketmaker.interface_adapter.ChartController;
import com.marketmaker.interface_adapter.OrderTicketController;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.interface_adapter.WatchlistController;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartResponseModel;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryResponseModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;
import com.marketmaker.use_case.view_positions.ViewPositionsResponseModel;

/**
 * The main window: watchlist on the left, chart in the middle, order ticket and
 * positions on the right, order history along the bottom.
 *
 * <p>Every panel is driven by a view model rather than reading data itself, so the
 * frame only owns layout. Presenters publish into those view models.
 */
public class DashboardFrame extends JFrame {

    private final ViewModel<ViewPortfolioSummaryResponseModel> summary;
    private final ViewModel<ViewPositionsResponseModel> positions;
    private final ViewModel<ViewOrderHistoryResponseModel> orderHistory;
    private final ViewModel<ViewCandlestickChartResponseModel> chart;
    private final ViewModel<List<Quote>> watchlist;
    private final OrderTicketPanel ticket;
    private final WatchlistController watchlistController;
    private final ChartController chartController;
    private final Runnable onRefresh;
    private final java.util.function.Consumer<Boolean> onLiveToggle;

    public DashboardFrame(OrderTicketController ticketController,
                          WatchlistController watchlistController,
                          ChartController chartController,
                          ViewModel<String> status,
                          Runnable onRefresh,
                          java.util.function.Consumer<Boolean> onLiveToggle,
                          ViewModel<ViewPortfolioSummaryResponseModel> summary,
                          ViewModel<ViewPositionsResponseModel> positions,
                          ViewModel<ViewOrderHistoryResponseModel> orderHistory,
                          ViewModel<ViewCandlestickChartResponseModel> chart,
                          ViewModel<List<Quote>> watchlist) {
        super("MarketMaker — Paper Trading Simulator");
        this.summary = summary;
        this.positions = positions;
        this.orderHistory = orderHistory;
        this.chart = chart;
        this.watchlist = watchlist;
        // Built before the chrome, because the toolbar's Buy and Sell drive it.
        this.watchlistController = watchlistController;
        this.chartController = chartController;
        this.onRefresh = onRefresh;
        this.onLiveToggle = onLiveToggle;
        this.ticket = new OrderTicketPanel(summary, status, ticketController);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 940);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.BAR_BG);
        root.add(buildNorth(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JComponent buildNorth() {
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(buildToolBar(), BorderLayout.NORTH);
        north.add(new AccountSummaryBar(summary), BorderLayout.SOUTH);
        return north;
    }

    private JComponent buildToolBar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(UiTheme.BAR_BG);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(5, 6, 5, 6)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        actions.setOpaque(false);
        // Buy and Sell are shortcuts into the ticket, which is where an order is actually
        // built — they set the side rather than placing anything on their own.
        JButton buy = ViewComponents.button("Buy");
        buy.addActionListener(event -> ticket.chooseSide(true));
        JButton sell = ViewComponents.button("Sell");
        sell.addActionListener(event -> ticket.chooseSide(false));
        JButton refresh = ViewComponents.button("Refresh");
        refresh.addActionListener(event -> onRefresh.run());
        actions.add(buy);
        actions.add(sell);
        actions.add(refresh);

        JCheckBox liveData = new JCheckBox("Live data", true);
        liveData.setFont(UiTheme.BASE);
        liveData.setOpaque(false);
        // Unticking stops the polling: useful when the free tier's rate limit is close.
        liveData.addActionListener(event -> onLiveToggle.accept(liveData.isSelected()));
        actions.add(liveData);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        status.setOpaque(false);
        status.add(ViewComponents.statusDot(UiTheme.GREEN));
        JLabel connected = new JLabel("Finnhub connected");
        connected.setFont(UiTheme.BASE);
        connected.setForeground(UiTheme.TEXT_MUTED);
        status.add(connected);

        toolbar.add(actions, BorderLayout.WEST);
        toolbar.add(status, BorderLayout.EAST);
        return toolbar;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setBackground(UiTheme.BAR_BG);
        body.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        body.add(new WatchlistPanel(watchlist, watchlistController), BorderLayout.WEST);
        body.add(new ChartPanel(chart, chartController), BorderLayout.CENTER);
        body.add(buildRightColumn(), BorderLayout.EAST);
        body.add(new OrderHistoryPanel(orderHistory), BorderLayout.SOUTH);
        return body;
    }

    private JComponent buildRightColumn() {
        JPanel column = new JPanel(new BorderLayout(0, 8));
        column.setOpaque(false);
        column.setPreferredSize(new Dimension(358, 593));
        column.add(ticket, BorderLayout.NORTH);
        column.add(new PositionsPanel(positions), BorderLayout.CENTER);
        return column;
    }
}
