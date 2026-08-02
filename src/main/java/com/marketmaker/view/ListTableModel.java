package com.marketmaker.view;

import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 * A read-only table model backed by a {@code List} of rows, one {@link Column} per
 * displayed field. One model serves every table in the app; panels only supply the
 * column list and call {@link #setRows}.
 *
 * @param <T> the row type, normally a use-case response row such as
 *            {@code OrderHistoryRow} or {@code PositionView}
 */
public class ListTableModel<T> extends AbstractTableModel {

    private final List<Column<T>> columns;
    private List<T> rows = List.of();

    public ListTableModel(List<Column<T>> columns) {
        this.columns = List.copyOf(columns);
    }

    /**
     * Replaces every row. Safe to call from any thread: presenters are driven by
     * interactors that may run off the event dispatch thread (the live quote feed
     * in particular), and Swing models may only be mutated on the EDT.
     *
     * @param newRows the rows to show; {@code null} is treated as empty
     */
    public void setRows(List<T> newRows) {
        final List<T> copy = newRows == null ? List.of() : List.copyOf(newRows);
        SwingUtilities.invokeLater(() -> {
            this.rows = copy;
            fireTableDataChanged();
        });
    }

    public CellStyle styleAt(int column) {
        return columns.get(column).style();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).name();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columns.get(column).type();
    }

    @Override
    public Object getValueAt(int row, int column) {
        return columns.get(column).value().apply(rows.get(row));
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
