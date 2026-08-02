package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.time.Instant;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_order_history.OrderHistoryRow;
import com.marketmaker.use_case.view_order_history.TradeHistoryRow;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryResponseModel;

/**
 * Order and trade history. The use case returns both lists, so each gets a tab:
 * the orders that were placed with their status, and the trades they produced.
 */
public class OrderHistoryPanel extends TitledPanel {

    private static final int STATUS_COLUMN = 6;

    private static final List<Column<OrderHistoryRow>> ORDER_COLUMNS = List.of(
            Column.of("Time", Instant.class, OrderHistoryRow::getTimestamp),
            Column.of("Symbol", String.class, OrderHistoryRow::getTicker),
            new Column<>("Side", Object.class, OrderHistoryRow::getSide, CellStyle.SIDE),
            Column.of("Type", Object.class, OrderHistoryRow::getType),
            new Column<>("Qty", Integer.class, OrderHistoryRow::getQuantity, CellStyle.NUMBER),
            new Column<>("Lmt / Stop", Double.class, OrderHistoryRow::getLimitOrStopPrice, CellStyle.NUMBER),
            new Column<>("Status", Object.class, OrderHistoryRow::getStatus, CellStyle.STATUS));

    private static final List<Column<TradeHistoryRow>> TRADE_COLUMNS = List.of(
            Column.of("Time", Instant.class, TradeHistoryRow::getTimestamp),
            Column.of("Symbol", String.class, TradeHistoryRow::getTicker),
            new Column<>("Side", Object.class, TradeHistoryRow::getSide, CellStyle.SIDE),
            new Column<>("Qty", Integer.class, TradeHistoryRow::getQuantity, CellStyle.NUMBER),
            new Column<>("Price", Double.class, TradeHistoryRow::getPrice, CellStyle.NUMBER),
            new Column<>("Realized P/L", Double.class, TradeHistoryRow::getRealizedPnL, CellStyle.SIGNED));

    private final ListTableModel<OrderHistoryRow> orderModel = new ListTableModel<>(ORDER_COLUMNS);
    private final ListTableModel<TradeHistoryRow> tradeModel = new ListTableModel<>(TRADE_COLUMNS);
    private final TableRowSorter<ListTableModel<OrderHistoryRow>> orderSorter;
    private final JLabel message = new JLabel();

    public OrderHistoryPanel(ViewModel<ViewOrderHistoryResponseModel> viewModel) {
        super("Order & Trade History");
        setPreferredSize(new Dimension(1440, 206));

        JTable orders = Tables.create(orderModel);
        orderSorter = new TableRowSorter<>(orderModel);
        orders.setRowSorter(orderSorter);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UiTheme.BASE);
        tabs.addTab("Orders", Tables.scroll(orders));
        tabs.addTab("Trades", Tables.scroll(Tables.create(tradeModel)));

        getContent().add(buildFilterRow(), BorderLayout.NORTH);
        getContent().add(tabs, BorderLayout.CENTER);

        viewModel.onState(response -> {
            orderModel.setRows(response.getOrders());
            tradeModel.setRows(response.getTrades());
        });
        viewModel.onError(this::showError);
    }

    private JPanel buildFilterRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filters.setOpaque(false);
        filters.add(filterButton("All Orders", null));
        filters.add(filterButton("Pending", "PENDING"));
        filters.add(filterButton("Filled", "FILLED"));
        filters.add(filterButton("Cancelled", "CANCELED"));

        message.setFont(UiTheme.BASE_ITALIC);
        message.setForeground(UiTheme.TEXT_LABEL);
        message.setText("Persisted — restored on relaunch");

        row.add(filters, BorderLayout.WEST);
        row.add(message, BorderLayout.EAST);
        return row;
    }

    /**
     * @param status the {@code Order.Status} name to keep, or {@code null} to show every order
     */
    private JButton filterButton(String text, String status) {
        JButton button = new JButton(text);
        button.setFont(UiTheme.BASE);
        button.setMargin(new Insets(3, 9, 3, 9));
        button.addActionListener(event -> orderSorter.setRowFilter(
                status == null ? null : RowFilter.regexFilter("^" + status + "$", STATUS_COLUMN)));
        return button;
    }

    private void showError(String errorMessage) {
        message.setText(errorMessage);
        message.setForeground(UiTheme.RED);
    }
}
