package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marketmaker.use_case.watchlist.WatchlistResponseModel;
import com.marketmaker.interface_adapter.CancelOrderController;
import com.marketmaker.interface_adapter.ChartController;
import com.marketmaker.interface_adapter.ProfileController;
import com.marketmaker.interface_adapter.RealizedPnLController;
import com.marketmaker.interface_adapter.OrderTicketController;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.interface_adapter.WatchlistController;
import com.marketmaker.use_case.user_profile.ViewProfileResponseModel;
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
    private final ViewModel<WatchlistResponseModel> watchlist;
    private final OrderTicketPanel ticket;
    private final OrderHistoryPanel history;
    private final ProfileDialog profileDialog;
    // Starts unknown rather than connected: the first refresh has not answered yet, and a
    // green dot before anyone has spoken to Finnhub is a claim the app cannot make.
    private final JLabel feedDot = ViewComponents.statusDot(UiTheme.TEXT_LABEL);
    private final JLabel feedLabel = new JLabel("Connecting to Finnhub…");
    private final WatchlistController watchlistController;
    private final ChartController chartController;
    private final Runnable onRefresh;
    private final java.util.function.Consumer<Boolean> onLiveToggle;

    public DashboardFrame(OrderTicketController ticketController,
                          WatchlistController watchlistController,
                          ChartController chartController,
                          CancelOrderController cancelController,
                          RealizedPnLController realizedPnLController,
                          ProfileController profileController,
                          ViewModel<String> status,
                          ViewModel<ViewProfileResponseModel> profile,
                          Runnable onRefresh,
                          java.util.function.Consumer<Boolean> onLiveToggle,
                          ViewModel<ViewPortfolioSummaryResponseModel> summary,
                          ViewModel<ViewPositionsResponseModel> positions,
                          ViewModel<ViewOrderHistoryResponseModel> orderHistory,
                          ViewModel<ViewCandlestickChartResponseModel> chart,
                          ViewModel<WatchlistResponseModel> watchlist) {
        super("MarketMaker - Paper Trading Simulator");
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
        this.ticket = new OrderTicketPanel(summary, status, watchlist,
                ticketController, realizedPnLController);
        this.history = new OrderHistoryPanel(orderHistory, status, cancelController);
        this.profileDialog = new ProfileDialog(this, profile, profileController);
        // The watchlist is the only thing that talks to the feed every tick, so its outcome
        // is what the status light knows about.
        watchlist.onState(watched -> setFeedStatus(UiTheme.GREEN, "Finnhub connected", null));
        watchlist.onError(problem -> setFeedStatus(UiTheme.AMBER, "Finnhub - no prices", problem));

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

    private void setFeedStatus(java.awt.Color colour, String text, String detail) {
        feedDot.setForeground(colour);
        feedLabel.setText(text);
        feedLabel.setToolTipText(detail);
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
        // built - they set the side rather than placing anything on their own.
        JButton buy = mnemonic(ViewComponents.button("Buy"), KeyEvent.VK_B, "Buy the selected symbol (Alt+B)");
        buy.addActionListener(event -> ticket.chooseSide(true));
        JButton sell = mnemonic(ViewComponents.button("Sell"), KeyEvent.VK_S, "Sell the selected symbol (Alt+S)");
        sell.addActionListener(event -> ticket.chooseSide(false));
        JButton refresh = mnemonic(ViewComponents.button("Refresh"), KeyEvent.VK_R, "Re-quote everything now (Alt+R)");
        refresh.addActionListener(event -> onRefresh.run());
        JButton profileButton = mnemonic(ViewComponents.button("Profile"), KeyEvent.VK_F,
                "Open the account profile (Alt+F)");
        profileButton.addActionListener(event -> profileDialog.open());
        actions.add(buy);
        actions.add(sell);
        actions.add(refresh);
        actions.add(profileButton);

        JCheckBox liveData = new JCheckBox("Live data", true);
        liveData.setFont(UiTheme.BASE);
        liveData.setOpaque(false);
        // Unticking stops the polling: useful when the free tier's rate limit is close.
        liveData.addActionListener(event -> onLiveToggle.accept(liveData.isSelected()));
        actions.add(liveData);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        status.setOpaque(false);
        status.add(feedDot);
        feedLabel.setFont(UiTheme.BASE);
        feedLabel.setForeground(UiTheme.TEXT_MUTED);
        status.add(feedLabel);

        toolbar.add(actions, BorderLayout.WEST);
        toolbar.add(status, BorderLayout.EAST);
        return toolbar;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setBackground(UiTheme.BAR_BG);
        body.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        body.add(new WatchlistPanel(watchlist, watchlistController, chartController), BorderLayout.WEST);
        body.add(new ChartPanel(chart, chartController), BorderLayout.CENTER);
        body.add(buildRightColumn(), BorderLayout.EAST);
        body.add(history, BorderLayout.SOUTH);
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

    /** Gives a toolbar button a keyboard route and says what the shortcut is. */
    private static JButton mnemonic(JButton button, int key, String tip) {
        button.setMnemonic(key);
        button.setToolTipText(tip);
        return button;
    }
}
