package com.marketmaker.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

/** The shared table engine behind every panel: column mapping, row replacement, sorting types. */
class ListTableModelTest {

    private record Row(String ticker, Double price) {
    }

    private static final List<Column<Row>> COLUMNS = List.of(
            Column.of("Symbol", String.class, Row::ticker),
            new Column<>("Price", Double.class, Row::price, CellStyle.NUMBER));

    /** setRows hops to the event dispatch thread, so tests must let it land. */
    private static void flushEventQueue() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> { });
    }

    @Test
    void startsEmptySoPanelsCanBeBuiltBeforeDataArrives() {
        ListTableModel<Row> model = new ListTableModel<>(COLUMNS);

        assertEquals(0, model.getRowCount());
        assertEquals(2, model.getColumnCount());
        assertEquals("Symbol", model.getColumnName(0));
    }

    @Test
    void readsEachColumnThroughItsExtractor() throws Exception {
        ListTableModel<Row> model = new ListTableModel<>(COLUMNS);

        model.setRows(List.of(new Row("AAPL", 229.35), new Row("NVDA", 121.44)));
        flushEventQueue();

        assertEquals(2, model.getRowCount());
        assertEquals("AAPL", model.getValueAt(0, 0));
        assertEquals(121.44, model.getValueAt(1, 1));
    }

    @Test
    void reportsRawTypesSoNumericColumnsSortNumerically() {
        ListTableModel<Row> model = new ListTableModel<>(COLUMNS);

        // Returning a formatted String here would sort "9.00" above "10.00".
        assertEquals(Double.class, model.getColumnClass(1));
        assertEquals(CellStyle.NUMBER, model.styleAt(1));
    }

    @Test
    void replacesRowsRatherThanAppending() throws Exception {
        ListTableModel<Row> model = new ListTableModel<>(COLUMNS);

        model.setRows(List.of(new Row("AAPL", 229.35), new Row("NVDA", 121.44)));
        model.setRows(List.of(new Row("TSLA", 242.15)));
        flushEventQueue();

        assertEquals(1, model.getRowCount());
        assertEquals("TSLA", model.getValueAt(0, 0));
    }

    @Test
    void treatsNullRowsAsEmptyRatherThanThrowing() throws Exception {
        ListTableModel<Row> model = new ListTableModel<>(COLUMNS);

        model.setRows(List.of(new Row("AAPL", 229.35)));
        model.setRows(null);
        flushEventQueue();

        assertEquals(0, model.getRowCount());
    }

    @Test
    void keepsNullCellValuesSoAbsentPricesRenderAsADash() throws Exception {
        ListTableModel<Row> model = new ListTableModel<>(COLUMNS);

        // A market order has no limit price; the renderer turns this null into "—".
        model.setRows(List.of(new Row("AAPL", null)));
        flushEventQueue();

        assertNull(model.getValueAt(0, 1));
    }

    @Test
    void staysReadOnly() {
        assertFalse(new ListTableModel<>(COLUMNS).isCellEditable(0, 0));
    }
}
