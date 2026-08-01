package com.marketmaker.view;

import com.marketmaker.use_case.order_history.OrderHistoryDataAccessInterface;
import com.marketmaker.use_case.order_history.OrderHistoryEntry;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

public class OrderHistoryView extends JPanel {

    private static final String[] COLUMNS = {
            "Time", "Symbol", "Side", "Type", "Qty", "Limit/Stop", "Fill/Status", "Realized P/L"
    };

    public OrderHistoryView(OrderHistoryDataAccessInterface dataAccess) {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Order & Trade History"));

        DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<OrderHistoryEntry> rows = dataAccess.loadAll();
        for (OrderHistoryEntry entry : rows) {
            model.addRow(new Object[]{
                    entry.getTime(),
                    entry.getSymbol(),
                    entry.getSide(),
                    entry.getType(),
                    entry.getQuantity(),
                    entry.getLimitOrStop(),
                    entry.getFillStatus(),
                    entry.getRealizedPl()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new JLabel("  Persisted — restored on relaunch"), BorderLayout.SOUTH);
    }
}
