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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.marketmaker.interface_adapter.user_profile.ViewProfileController;
import com.marketmaker.interface_adapter.user_profile.ViewProfileState;
import com.marketmaker.interface_adapter.user_profile.ViewProfileViewModel;

/** Shows cash, live-valued holdings, and total equity for one account. */
public class ProfileView extends JPanel implements PropertyChangeListener {
    private static final String[] COLUMNS = {"Ticker", "Shares", "Avg Cost", "Last", "Market Value", "Unrealized P/L"};

    private final ViewProfileController controller;
    private final JTextField accountField = new JTextField(10);
    private final JLabel summaryLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };

    public ProfileView(ViewProfileController controller, ViewProfileViewModel viewModel) {
        super(new BorderLayout());
        this.controller = controller;
        viewModel.addPropertyChangeListener(this);
        setBorder(BorderFactory.createTitledBorder("Profile"));

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(event -> load(accountField.getText().trim()));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Account:"));
        controls.add(accountField);
        controls.add(loadButton);

        JTable table = new JTable(tableModel);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);

        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(summaryLabel, BorderLayout.SOUTH);
    }

    /** Pre-fills the account and loads it. */
    public void start(String accountId) {
        accountField.setText(accountId);
        load(accountId);
    }

    /**
     * Every holding costs a quote, so pricing a portfolio is several HTTP round trips.
     * Off the event dispatch thread, or the window freezes for the length of them.
     */
    private void load(String accountId) {
        Background.run(() -> controller.view(accountId));
    }

    /**
     * Re-reads the profile after a trade elsewhere in the app, but only when this screen is
     * already showing that account — otherwise the table would swap out from under the user
     * while the account field still named someone else.
     */
    public void refreshIfShowing(String accountId) {
        if (accountField.getText().trim().equals(accountId)) {
            load(accountId);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // Presenters fire from the background worker; touch Swing only on the EDT.
        ViewProfileState state = (ViewProfileState) event.getNewValue();
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (String[] row : state.getHoldings()) {
                tableModel.addRow(row);
            }

            if (!state.getError().isEmpty()) {
                summaryLabel.setText("  " + state.getError());
            } else {
                summaryLabel.setText(String.format("  %s — cash %s, total equity %s, day P/L %s",
                        state.getUserName(), state.getCashBalance(), state.getTotalEquity(),
                        state.getDailyPnL()));
            }
        });
    }
}
