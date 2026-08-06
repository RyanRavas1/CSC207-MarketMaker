package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.marketmaker.interface_adapter.order_history.OrderHistoryState;
import com.marketmaker.interface_adapter.order_history.OrderHistoryViewModel;
import com.marketmaker.interface_adapter.order_history.ViewOrderHistoryController;

/** Shows every order and trade the account has placed, newest first. */
public class OrderHistoryView extends JPanel implements PropertyChangeListener {
    private static final String[] COLUMNS = {
            "Time", "Symbol", "Side", "Type", "Qty", "Limit/Stop", "Fill/Status", "Realized P/L"
    };

    private final ViewOrderHistoryController controller;
    private final JLabel summaryLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private String accountId = "";

    public OrderHistoryView(ViewOrderHistoryController controller, OrderHistoryViewModel viewModel) {
        super(new BorderLayout());
        this.controller = controller;
        viewModel.addPropertyChangeListener(this);
        setBorder(BorderFactory.createTitledBorder("Order & Trade History"));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> reload());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(refreshButton);

        JTable table = new JTable(tableModel);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);

        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(summaryLabel, BorderLayout.SOUTH);
    }

    /** Names the account whose history this panel shows, and loads it. */
    public void start(String accountId) {
        this.accountId = accountId;
        reload();
    }

    /** Re-reads the log after an order is placed, filled or cancelled elsewhere in the app. */
    public void refreshFor(String accountId) {
        this.accountId = accountId;
        reload();
    }

    // Reading the log goes through the same worker as every other use case, so the window
    // stays responsive when the account is loaded from disk.
    private void reload() {
        String requested = accountId;
        Background.run(() -> controller.view(requested));
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // Presenters fire from the background worker; touch Swing only on the EDT.
        OrderHistoryState state = (OrderHistoryState) event.getNewValue();
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (String[] row : state.getRows()) {
                tableModel.addRow(row);
            }

            if (!state.getError().isEmpty()) {
                summaryLabel.setText("  " + state.getError());
            } else {
                summaryLabel.setText(String.format("  %d order(s) — realized P/L %s — saved locally",
                        state.getRows().size(), state.getTotalRealizedPnL()));
            }
        });
    }
}
