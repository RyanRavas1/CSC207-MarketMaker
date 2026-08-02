package com.marketmaker.view;

/** How {@link StyledCellRenderer} should draw a column's values. */
public enum CellStyle {

    /** Left-aligned text. */
    PLAIN,

    /** Right-aligned number, two decimal places. */
    NUMBER,

    /** Right-aligned number, signed and coloured green/red. */
    SIGNED,

    /** BUY in green, SELL in red. */
    SIDE,

    /** Order status drawn as a tinted pill. */
    STATUS
}
