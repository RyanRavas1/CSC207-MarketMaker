package com.marketmaker.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

/**
 * Guards the renderer wiring. JTable keeps an exact {@code Double.class} entry in its
 * default renderer registry, so anything registered against {@code Number} loses and
 * prices quietly revert to Swing's own formatting — 228.9 instead of 228.90.
 */
class TablesTest {

    private record Row(String ticker, Double price, Double profit) {
    }

    private static final List<Column<Row>> COLUMNS = List.of(
            Column.of("Symbol", String.class, Row::ticker),
            new Column<>("Price", Double.class, Row::price, CellStyle.NUMBER),
            new Column<>("P/L", Double.class, Row::profit, CellStyle.SIGNED));

    private static JTable tableWith(Row row) throws Exception {
        ListTableModel<Row> model = new ListTableModel<>(COLUMNS);
        model.setRows(List.of(row));
        SwingUtilities.invokeAndWait(() -> { });
        return Tables.create(model);
    }

    private static String rendered(JTable table, int column) {
        Component cell = table.getCellRenderer(0, column).getTableCellRendererComponent(
                table, table.getValueAt(0, column), false, false, 0, column);
        return ((JLabel) cell).getText();
    }

    @Test
    void keepsTwoDecimalsOnPricesInsteadOfSwingsOwnNumberFormat() throws Exception {
        JTable table = tableWith(new Row("AAPL", 228.90, 157.50));

        assertEquals("228.90", rendered(table, 1));
    }

    @Test
    void signsProfitAndLossColumns() throws Exception {
        JTable table = tableWith(new Row("TSLA", 242.15, -78.50));

        assertEquals("-78.50", rendered(table, 2));
    }

    @Test
    void drawsAbsentValuesAsADash() throws Exception {
        JTable table = tableWith(new Row("SPY", null, null));

        assertEquals(Format.ABSENT, rendered(table, 1));
    }

    @Test
    void scrollsSoLongListsAreNotClippedByThePanel() {
        JTable table = new JTable();

        assertTrue(Tables.scroll(table).getViewport().getView() == table);
    }
}
