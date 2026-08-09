package com.marketmaker.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/** Builds the dashboard's tables so every one of them scrolls, sorts and looks the same. */
public final class Tables {

    /** Breathing room either side of a column's widest value. */
    private static final int COLUMN_PADDING = 14;

    private Tables() {
    }

    public static <T> JTable create(ListTableModel<T> model) {
        return create(model, null);
    }

    /**
     * @param accessibleName what a screen reader should call this table; a bare JTable is
     *                       announced without saying which of the four it is
     */
    public static <T> JTable create(ListTableModel<T> model, String accessibleName) {
        // getCellRenderer is overridden rather than registering defaults per column class:
        // JTable keeps exact entries for Double and Float that beat anything registered
        // against Number, which silently reverted prices to its own one-decimal format.
        final StyledCellRenderer renderer = new StyledCellRenderer();
        JTable table = new JTable(model) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                return renderer;
            }
        };
        table.setFont(UiTheme.BASE);
        table.setRowHeight(20);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(UiTheme.PANEL_BG);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        if (accessibleName != null) {
            table.getAccessibleContext().setAccessibleName(accessibleName);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(UiTheme.BASE_BOLD);
        header.setForeground(UiTheme.TEXT);
        header.setBackground(UiTheme.BAR_BG);
        header.setReorderingAllowed(false);

        // Columns are sized from content, so a narrow panel scrolls rather than
        // truncating "6,880.50" to "6,88...". Re-run whenever the rows or the
        // available width change.
        // Deferred: model listeners fire before JTable has refreshed its RowSorter, so
        // measuring rows here directly would read a stale row count.
        model.addTableModelListener(event -> SwingUtilities.invokeLater(() -> fitColumns(table)));
        table.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                fitColumns(table);
            }
        });
        return table;
    }

    /**
     * Widens each column to fit its header and every value in it, then fills the panel
     * if the total is narrower than the space available and scrolls sideways if not.
     */
    private static void fitColumns(JTable table) {
        int total = 0;
        for (int index = 0; index < table.getColumnCount(); index++) {
            TableColumn column = table.getColumnModel().getColumn(index);

            Component headerCell = table.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(table, column.getHeaderValue(), false, false, -1, index);
            int width = headerCell.getPreferredSize().width;

            for (int row = 0; row < table.getRowCount(); row++) {
                Component cell = table.prepareRenderer(table.getCellRenderer(row, index), row, index);
                width = Math.max(width, cell.getPreferredSize().width);
            }

            width += COLUMN_PADDING;
            column.setPreferredWidth(width);
            total += width;
        }

        Container parent = table.getParent();
        int available = parent == null ? 0 : parent.getWidth();
        table.setAutoResizeMode(total <= available
                ? JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS : JTable.AUTO_RESIZE_OFF);
    }

    /**
     * Wraps a table so long lists scroll instead of being clipped by the panel bounds.
     * Every table in the dashboard goes through here.
     */
    public static JScrollPane scroll(JTable table) {
        JScrollPane pane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_LIGHT));
        pane.getViewport().setBackground(UiTheme.PANEL_BG);
        return pane;
    }
}
