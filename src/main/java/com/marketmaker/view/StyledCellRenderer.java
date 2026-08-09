package com.marketmaker.view;

import java.awt.Color;
import java.awt.Component;
import java.time.Instant;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import com.marketmaker.interface_adapter.Format;

/**
 * Draws cells in the dashboard's colours: signed values green/red, BUY/SELL
 * coloured, order status as a tinted pill. Replaces the per-cell {@code JLabel}
 * factories the panels used before they became real tables.
 */
public class StyledCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        final CellStyle style = styleOf(table, column);
        setFont(style == CellStyle.SIGNED || style == CellStyle.SIDE ? UiTheme.BASE_BOLD : UiTheme.BASE);
        setHorizontalAlignment(style == CellStyle.NUMBER || style == CellStyle.SIGNED
                ? SwingConstants.RIGHT : SwingConstants.LEFT);
        setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
        setText(text(value, style));

        // Selection colours win, otherwise the style decides.
        setOpaque(true);
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        }
        else {
            setForeground(foreground(value, style));
            setBackground(background(value, style));
            if (style == CellStyle.STATUS) {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        }
        return this;
    }

    private static CellStyle styleOf(JTable table, int viewColumn) {
        final int modelColumn = table.convertColumnIndexToModel(viewColumn);
        if (table.getModel() instanceof ListTableModel<?> model) {
            return model.styleAt(modelColumn);
        }
        return CellStyle.PLAIN;
    }

    private static String text(Object value, CellStyle style) {
        if (value == null) {
            return Format.ABSENT;
        }
        if (value instanceof Instant instant) {
            return Format.time(instant);
        }
        if (value instanceof Enum<?> constant) {
            return style == CellStyle.SIDE ? constant.name() : Format.enumLabel(constant);
        }
        if (value instanceof Double number) {
            return style == CellStyle.SIGNED ? Format.signedMoney(number) : Format.money(number);
        }
        return String.valueOf(value);
    }

    private static Color foreground(Object value, CellStyle style) {
        switch (style) {
            case SIGNED:
                return value instanceof Double number ? sign(number) : UiTheme.TEXT;
            case SIDE:
                return "SELL".equals(name(value)) ? UiTheme.RED : UiTheme.GREEN;
            case STATUS:
                return statusForeground(name(value));
            default:
                return UiTheme.TEXT;
        }
    }

    private static Color background(Object value, CellStyle style) {
        if (style != CellStyle.STATUS) {
            return UiTheme.PANEL_BG;
        }
        switch (name(value)) {
            case "FILLED":
                return UiTheme.GREEN_TINT;
            case "CANCELED":
            case "CANCELLED":
                return UiTheme.RED_TINT;
            default:
                return UiTheme.AMBER_TINT;
        }
    }

    private static Color statusForeground(String status) {
        switch (status) {
            case "FILLED":
                return UiTheme.GREEN;
            case "CANCELED":
            case "CANCELLED":
                return UiTheme.RED;
            default:
                return UiTheme.AMBER;
        }
    }

    private static Color sign(double value) {
        if (value > 0) {
            return UiTheme.GREEN;
        }
        if (value < 0) {
            return UiTheme.RED;
        }
        return UiTheme.TEXT_MUTED;
    }

    private static String name(Object value) {
        if (value instanceof Enum<?> constant) {
            return constant.name();
        }
        return value == null ? "" : String.valueOf(value).toUpperCase();
    }
}
