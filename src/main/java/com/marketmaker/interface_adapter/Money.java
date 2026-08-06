package com.marketmaker.interface_adapter;

/** The one place a dollar amount becomes a string, so every screen shows money the same way. */
public final class Money {

    private Money() {
    }

    // Unrealized P/L goes negative, and "$-12.34" reads as a typo; the sign belongs out front.
    public static String format(double amount) {
        return amount < 0 ? String.format("-$%.2f", -amount) : String.format("$%.2f", amount);
    }
}
