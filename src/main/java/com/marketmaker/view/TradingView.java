package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import com.marketmaker.entities.Order;
import com.marketmaker.interface_adapter.trading.CancelOrderController;
import com.marketmaker.interface_adapter.trading.PlaceLimitStopOrderController;
import com.marketmaker.interface_adapter.trading.PlaceOrderController;
import com.marketmaker.interface_adapter.trading.TradingState;
import com.marketmaker.interface_adapter.trading.TradingViewModel;

/** Order ticket plus the pending orders it produced. */
public class TradingView extends JPanel implements PropertyChangeListener {
    private static final String[] COLUMNS = {"Order ID", "Ticker", "Type", "Trigger"};

    private final PlaceOrderController placeOrderController;
    private final PlaceLimitStopOrderController limitStopController;
    private final CancelOrderController cancelController;

    private final JTextField accountField = new JTextField("demo", 8);
    private final JTextField tickerField = new JTextField(6);
    private final JTextField quantityField = new JTextField("1", 4);
    private final JTextField triggerField = new JTextField(6);
    private final JComboBox<Order.Side> sideBox = new JComboBox<>(Order.Side.values());
    private final JComboBox<Order.Type> typeBox = new JComboBox<>(Order.Type.values());
    private final JLabel messageLabel = new JLabel(" ");
    private final JTable pendingTable;
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };

    public TradingView(PlaceOrderController placeOrderController,
                       PlaceLimitStopOrderController limitStopController,
                       CancelOrderController cancelController,
                       TradingViewModel viewModel) {
        super(new BorderLayout());
        this.placeOrderController = placeOrderController;
        this.limitStopController = limitStopController;
        this.cancelController = cancelController;
        viewModel.addPropertyChangeListener(this);
        setBorder(BorderFactory.createTitledBorder("Trading"));

        JButton placeButton = new JButton("Place");
        placeButton.addActionListener(event -> place());

        JButton cancelButton = new JButton("Cancel selected");
        cancelButton.addActionListener(event -> cancelSelected());

        // One wide FlowLayout row wraps when the panel is narrow, and its parent only ever
        // grants the unwrapped one-row height, so the overflow gets clipped away. Stacking
        // short rows in a BoxLayout keeps every control reachable in the shell's 380px column.
        JPanel ticket = new JPanel();
        ticket.setLayout(new BoxLayout(ticket, BoxLayout.Y_AXIS));
        ticket.add(row(new JLabel("Account:"), accountField));
        ticket.add(row(new JLabel("Ticker:"), tickerField));
        ticket.add(row(sideBox, typeBox));
        ticket.add(row(new JLabel("Qty:"), quantityField, new JLabel("Trigger:"), triggerField));
        ticket.add(row(placeButton, cancelButton));

        // The pending table holds whatever the presenters pushed, which belongs to whichever
        // account placed those orders. Retyping the account would otherwise leave the previous
        // account's rows on screen, where cancelling them fails with "Order not found."
        accountField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) { viewModel.clearPendingOrders(); }

            @Override
            public void removeUpdate(DocumentEvent event) { viewModel.clearPendingOrders(); }

            @Override
            public void changedUpdate(DocumentEvent event) { viewModel.clearPendingOrders(); }
        });

        pendingTable = new JTable(tableModel);
        pendingTable.setRowHeight(22);
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingTable.getTableHeader().setReorderingAllowed(false);

        add(ticket, BorderLayout.NORTH);
        add(new JScrollPane(pendingTable), BorderLayout.CENTER);
        add(messageLabel, BorderLayout.SOUTH);
    }

    /** One left-aligned line of the ticket; each row reports its own height, so nothing clips. */
    private static JPanel row(JComponent... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JComponent component : components) {
            panel.add(component);
        }
        return panel;
    }

    /** Reads the ticket and routes to the market or the limit/stop use case. */
    private void place() {
        String ticker = tickerField.getText().trim().toUpperCase();
        if (ticker.isEmpty()) {
            messageLabel.setText("  Enter a ticker.");
            return;
        }

        int quantity;
        double trigger;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            // Only limit and stop orders read this field, so a blank is fine for market orders.
            String triggerText = triggerField.getText().trim();
            trigger = triggerText.isEmpty() ? 0.0 : Double.parseDouble(triggerText);
        } catch (NumberFormatException exception) {
            messageLabel.setText("  Quantity and trigger must be numbers.");
            return;
        }

        String accountId = accountField.getText().trim();
        Order.Side side = (Order.Side) sideBox.getSelectedItem();
        Order.Type type = (Order.Type) typeBox.getSelectedItem();
        // A market order prices itself off the live feed, so it goes to the worker like
        // everything else — and the limit/stop path follows it to keep order sequencing.
        if (type == Order.Type.MARKET) {
            Background.run(() -> placeOrderController.place(accountId, ticker, side, quantity));
        } else {
            Background.run(() -> limitStopController.place(accountId, ticker, side, type, quantity, trigger));
        }
    }

    private void cancelSelected() {
        int row = pendingTable.getSelectedRow();
        if (row < 0) {
            messageLabel.setText("  Select a pending order first.");
            return;
        }
        String accountId = accountField.getText().trim();
        String orderId = (String) tableModel.getValueAt(row, 0);
        Background.run(() -> cancelController.cancel(accountId, orderId));
    }

    /** The account the driver in Main should poll quotes for. */
    public String getAccountId() { return accountField.getText().trim(); }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // Presenters fire from the background worker; touch Swing only on the EDT.
        TradingState state = (TradingState) event.getNewValue();
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (String[] row : state.getPendingOrders()) {
                tableModel.addRow(row);
            }

            String cash = state.getCashBalance().isEmpty() ? "" : "  |  cash " + state.getCashBalance();
            messageLabel.setText("  " + state.getMessage() + cash);
        });
    }
}
