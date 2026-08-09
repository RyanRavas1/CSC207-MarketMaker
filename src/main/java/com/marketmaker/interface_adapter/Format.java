package com.marketmaker.interface_adapter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Turns the raw values carried by use-case response models into display strings.
 * Formatting lives here, in the view, so the use-case layer stays free of
 * presentation decisions like which dash to show for an absent price.
 */
public final class Format {

    /** Shown wherever a value is genuinely absent, e.g. the limit price of a market order. */
    public static final String ABSENT = "-";

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private Format() {
    }

    public static String money(Double value) {
        return value == null ? ABSENT : String.format("%,.2f", value);
    }

    public static String signedMoney(Double value) {
        if (value == null) {
            return ABSENT;
        }
        return String.format("%s%,.2f", value < 0 ? "-" : "+", Math.abs(value));
    }

    /** Signed currency, with the sign outside the symbol: {@code +$342.18}, {@code -$78.50}. */
    public static String signedDollars(Double value) {
        if (value == null) {
            return ABSENT;
        }
        return String.format("%s$%,.2f", value < 0 ? "-" : "+", Math.abs(value));
    }

    /** Share volume, abbreviated the way a trading screen shows it: {@code 18.2M}. */
    public static String volume(Double value) {
        if (value == null) {
            return ABSENT;
        }
        double magnitude = Math.abs(value);
        if (magnitude >= 1_000_000_000) {
            return String.format("%.2fB", value / 1_000_000_000);
        }
        if (magnitude >= 1_000_000) {
            return String.format("%.2fM", value / 1_000_000);
        }
        if (magnitude >= 1_000) {
            return String.format("%.1fK", value / 1_000);
        }
        return String.format("%,.0f", value);
    }

    public static String percent(Double value) {
        if (value == null) {
            return ABSENT;
        }
        return String.format("%s%.2f%%", value < 0 ? "-" : "+", Math.abs(value));
    }

    public static String time(Instant value) {
        return value == null ? ABSENT : TIME.format(value);
    }

    /** Title case for enum constants, so {@code STOP_LOSS} reads as "Stop Loss". */
    public static String enumLabel(Enum<?> value) {
        if (value == null) {
            return ABSENT;
        }
        String[] words = value.name().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(word.charAt(0)).append(word.substring(1).toLowerCase());
        }
        return out.toString();
    }
}
