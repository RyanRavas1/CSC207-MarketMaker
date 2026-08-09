package com.marketmaker.view;

import java.util.function.Function;

/**
 * One column of a {@link ListTableModel}.
 *
 * <p>{@code value} must return the <em>raw</em> value (a {@code Double}, {@code Instant},
 * enum, ...) and never a pre-formatted string — {@link javax.swing.table.TableRowSorter}
 * sorts on what this returns, so formatting here would sort "9.00" above "10.00".
 * {@link StyledCellRenderer} does the formatting at paint time instead.
 *
 * @param <T>   the row type this column reads from
 * @param name  the column heading
 * @param type  the class of the raw value, used by the sorter
 * @param value reads the raw value out of a row
 * @param style how {@link StyledCellRenderer} should paint the value
 */
public record Column<T>(String name, Class<?> type, Function<T, Object> value, CellStyle style) {

    public static <T> Column<T> of(String name, Class<?> type, Function<T, Object> value) {
        return new Column<>(name, type, value, CellStyle.PLAIN);
    }
}
