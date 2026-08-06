package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * The application shell: the background window that hosts every container.
 * <p>
 * This class owns only layout and chrome (menu bar, toolbar, and the
 * {@link BorderLayout} that positions each panel). The individual containers
 * are injected as {@link JComponent}s so the shell stays decoupled from their
 * data-access wiring. Slots that aren't built yet are filled with
 * placeholders and will be swapped for real panels on respective branches.
 */
public class MainWindow extends JFrame {
    private static final Color CONNECTED_GREEN = new Color(0x2E, 0x7D, 0x32);

    private final JComponent watchlistPanel;
    private final JComponent orderTicketPanel;
    private final JComponent positionsPanel;
    private final JComponent accountSummaryPanel;

    /**
     * @param orderHistoryPanel the finished Order &amp; Trade History container,
     *                          wired by the caller and dropped into the SOUTH slot
     */
    public MainWindow(JComponent orderHistoryPanel) {
        this(null, null, null, orderHistoryPanel, null);
    }

    /**
     * Every slot takes the real container, or null to keep its "coming soon" placeholder.
     * The chart slot has no implementation yet, so it stays a placeholder.
     */
    public MainWindow(JComponent watchlistPanel, JComponent orderTicketPanel,
                      JComponent positionsPanel, JComponent orderHistoryPanel,
                      JComponent accountSummaryPanel) {
        super("MarketMaker — Paper Trading Simulator");
        this.watchlistPanel = watchlistPanel;
        this.orderTicketPanel = orderTicketPanel;
        this.positionsPanel = positionsPanel;
        this.accountSummaryPanel = accountSummaryPanel;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(buildMenuBar());

        setLayout(new BorderLayout());
        add(buildNorth(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        // BorderLayout gives SOUTH whatever height it asks for, and a table-backed panel asks
        // for a lot, which starved the centre row. Cap it so the trading column keeps its space.
        add(slot(orderHistoryPanel, "Order & Trade History", 0, 230), BorderLayout.SOUTH);

        setSize(1450, 900);
        setLocationRelativeTo(null);
    }

    // Menu bar

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.add(buildAccountMenu());
        bar.add(buildTradeMenu());
        bar.add(buildViewMenu());
        bar.add(buildHelpMenu());
        return bar;
    }

    private JMenu buildAccountMenu() {
        JMenu menu = new JMenu("Account");
        JMenuItem notSignedIn = new JMenuItem("Not signed in");
        notSignedIn.setEnabled(false);
        menu.add(notSignedIn);
        menu.addSeparator();
        menu.add(new JMenuItem("Sign In…"));
        menu.add(new JMenuItem("Create Account…"));
        menu.addSeparator();
        JMenuItem profile = new JMenuItem("Profile");
        profile.setEnabled(false);
        menu.add(profile);
        JMenuItem prefs = new JMenuItem("Preferences…");
        prefs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_DOWN_MASK));
        menu.add(prefs);
        menu.addSeparator();
        JMenuItem signOut = new JMenuItem("Sign Out");
        signOut.setEnabled(false);
        menu.add(signOut);
        return menu;
    }

    private JMenu buildTradeMenu() {
        JMenu menu = new JMenu("Trade");
        JMenuItem buy = new JMenuItem("Buy…");
        buy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        menu.add(buy);
        JMenuItem sell = new JMenuItem("Sell…");
        sell.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        menu.add(sell);
        menu.addSeparator();
        JMenuItem cancelSelected = new JMenuItem("Cancel Selected Order");
        cancelSelected.setEnabled(false);
        menu.add(cancelSelected);
        menu.add(new JMenuItem("Cancel All Pending"));
        menu.addSeparator();
        menu.add(new JMenuItem("Reset Simulation…"));
        return menu;
    }

    private JMenu buildViewMenu() {
        JMenu menu = new JMenu("View");
        menu.add(new JCheckBoxMenuItem("Watchlist", true));
        menu.add(new JCheckBoxMenuItem("Positions", true));
        menu.add(new JCheckBoxMenuItem("Order Ticket", true));
        menu.add(new JCheckBoxMenuItem("Order & Trade History", true));
        menu.addSeparator();

        JMenu interval = new JMenu("Chart Interval");
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem oneMin = new JRadioButtonMenuItem("1 Minute");
        JRadioButtonMenuItem fiveMin = new JRadioButtonMenuItem("5 Minutes", true);
        JRadioButtonMenuItem oneDay = new JRadioButtonMenuItem("1 Day");
        group.add(oneMin);
        group.add(fiveMin);
        group.add(oneDay);
        interval.add(oneMin);
        interval.add(fiveMin);
        interval.add(oneDay);
        menu.add(interval);

        JMenuItem refresh = new JMenuItem("Refresh Data");
        refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        menu.add(refresh);
        return menu;
    }

    private JMenu buildHelpMenu() {
        JMenu menu = new JMenu("Help");
        JMenuItem docs = new JMenuItem("Documentation");
        docs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menu.add(docs);
        menu.add(new JMenuItem("Keyboard Shortcuts…"));
        menu.add(new JMenuItem("Finnhub API Status"));
        menu.addSeparator();
        menu.add(new JMenuItem("About MarketMaker…"));
        return menu;
    }

    // Toolbar + summary strip (NORTH)

    private JPanel buildNorth() {
        JPanel north = new JPanel(new BorderLayout());
        north.add(buildToolBar(), BorderLayout.NORTH);
        north.add(slot(accountSummaryPanel, "Account Summary", 0, 46), BorderLayout.SOUTH);
        return north;
    }

    private JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(new JButton("Buy"));
        toolBar.add(new JButton("Sell"));
        toolBar.add(new JButton("Refresh"));
        toolBar.addSeparator();
        toolBar.add(new JCheckBox("Live data", true));
        toolBar.add(Box.createHorizontalGlue());
        JLabel status = new JLabel("● Finnhub connected  ");
        status.setForeground(CONNECTED_GREEN);
        toolBar.add(status);
        return toolBar;
    }

    // Center region: watchlist | chart | ticket+positions

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.add(slot(watchlistPanel, "Watchlist", 300, 0), BorderLayout.WEST);
        center.add(placeholder("Chart", 0, 0), BorderLayout.CENTER);
        center.add(buildEastColumn(), BorderLayout.EAST);
        return center;
    }

    private JPanel buildEastColumn() {
        JPanel east = new JPanel(new BorderLayout());
        east.add(slot(orderTicketPanel, "Order Ticket", 430, 300), BorderLayout.CENTER);
        east.add(slot(positionsPanel, "Positions", 430, 240), BorderLayout.SOUTH);
        return east;
    }

    /** The real container at its slot size, or the placeholder while that panel doesn't exist. */
    private static JComponent slot(JComponent panel, String title, int prefWidth, int prefHeight) {
        if (panel == null) {
            return placeholder(title, prefWidth, prefHeight);
        }
        if (prefWidth > 0 || prefHeight > 0) {
            panel.setPreferredSize(new Dimension(prefWidth, prefHeight));
        }
        return panel;
    }

    // Shared placeholder for not-yet-built containers

    private static JPanel placeholder(String title, int prefWidth, int prefHeight) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        JLabel label = new JLabel("(" + title + " — coming soon)", SwingConstants.CENTER);
        label.setForeground(Color.GRAY);
        panel.add(label, BorderLayout.CENTER);
        if (prefWidth > 0 || prefHeight > 0) {
            panel.setPreferredSize(new Dimension(prefWidth, prefHeight));
        }
        return panel;
    }
}
