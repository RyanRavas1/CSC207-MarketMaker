package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import com.marketmaker.interface_adapter.watchlist.WatchlistController;
import com.marketmaker.interface_adapter.watchlist.WatchlistState;
import com.marketmaker.interface_adapter.watchlist.WatchlistViewModel;

/** Shows the watched tickers and their last quoted price. */
public class WatchlistView extends JPanel implements PropertyChangeListener {
    private static final String[] COLUMNS = {"Ticker", "Price"};
    // Slower than the feed's own cache window, so an idle watchlist re-polls rather than
    // re-serving the same cached quote over and over.
    private static final int REFRESH_INTERVAL_MS = 10_000;

    // ponytail: the watchlist lives in the view because the use case takes tickers per request.
    // Move it behind a WatchlistDAO when it needs to survive a restart.
    private final List<String> tickers = new ArrayList<>();
    private final WatchlistController controller;
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JLabel errorLabel = new JLabel(" ");
    private final JTable table = new JTable(tableModel);

    public WatchlistView(WatchlistController controller, WatchlistViewModel viewModel) {
        super(new BorderLayout());
        this.controller = controller;
        viewModel.addPropertyChangeListener(this);
        setBorder(BorderFactory.createTitledBorder("Watchlist"));

        JTextField tickerField = new JTextField(8);
        JButton addButton = new JButton("Add");
        JButton removeButton = new JButton("Remove");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(event -> {
            String ticker = tickerField.getText().trim().toUpperCase();
            if (ticker.isEmpty()) {
                errorLabel.setText("Enter a ticker.");
            } else if (tickers.contains(ticker)) {
                errorLabel.setText("Already watching " + ticker + ".");
            } else {
                tickers.add(ticker);
                tickerField.setText("");
                refresh();
            }
        });
        // A ticker the feed can't quote never gets a row, so removing it goes by the field
        // rather than the table — otherwise a typo'd symbol would be stuck in the list.
        removeButton.addActionListener(event -> {
            String typed = tickerField.getText().trim().toUpperCase();
            String ticker = typed.isEmpty() ? selectedTicker() : typed;
            if (ticker == null) {
                errorLabel.setText("Type or select a ticker to remove.");
            } else if (!tickers.remove(ticker)) {
                errorLabel.setText("Not watching " + ticker + ".");
            } else {
                tickerField.setText("");
                errorLabel.setText(" ");
                if (tickers.isEmpty()) {
                    tableModel.setRowCount(0);
                } else {
                    refresh();
                }
            }
        });
        refreshButton.addActionListener(event -> refresh());

        // Two short rows rather than one wide one, so nothing clips in the shell's 300px column.
        JPanel entry = new JPanel(new FlowLayout(FlowLayout.LEFT));
        entry.add(new JLabel("Ticker:"));
        entry.add(tickerField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(addButton);
        actions.add(removeButton);
        actions.add(refreshButton);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(entry);
        controls.add(actions);

        table.setRowHeight(22);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(errorLabel, BorderLayout.SOUTH);
    }

    /** The ticker on the highlighted row, or null when nothing is selected. */
    private String selectedTicker() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (String) tableModel.getValueAt(row, 0);
    }

    /** Seeds the list, pulls a first set of quotes, and keeps them coming. */
    public void start(List<String> startingTickers) {
        tickers.addAll(startingTickers);
        refresh();
        new Timer(REFRESH_INTERVAL_MS, event -> refresh()).start();
    }

    /**
     * Quoting a ticker is an HTTP round trip against a live feed, so it can't run on the
     * event dispatch thread — a slow or timing-out request would freeze the whole window.
     */
    private void refresh() {
        Background.run(() -> controller.refresh(List.copyOf(tickers)));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // Presenters fire from the background worker; touch Swing only on the EDT.
        WatchlistState state = (WatchlistState) event.getNewValue();
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (String[] row : state.getRows()) {
                tableModel.addRow(row);
            }
            errorLabel.setText(state.getError().isEmpty() ? " " : state.getError());
        });
    }
}
