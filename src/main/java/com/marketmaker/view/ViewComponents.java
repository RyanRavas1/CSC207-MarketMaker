package com.marketmaker.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

// Helpers for making the labels and buttons the panels are built from, so the
// styling doesn't get repeated everywhere.
public class ViewComponents {

    public static JLabel label(String text, Font font, Color colour, int align) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(colour);
        label.setHorizontalAlignment(align);
        return label;
    }

    public static JLabel body(String text) {
        return label(text, UiTheme.BASE, UiTheme.TEXT, SwingConstants.LEFT);
    }

    public static JLabel caption(String text) {
        return label(text, UiTheme.BASE, UiTheme.TEXT_MUTED, SwingConstants.LEFT);
    }

    public static JLabel header(String text, int align) {
        return label(text, UiTheme.BASE_BOLD, UiTheme.TEXT, align);
    }

    public static JLabel cell(String text, int align) {
        return label(text, UiTheme.BASE, UiTheme.TEXT, align);
    }

    // Green when the value starts with "+", red when it starts with "-".
    public static JLabel signedCell(String text, int align) {
        return label(text, UiTheme.BASE_BOLD, signColour(text), align);
    }

    public static JLabel sideCell(String text) {
        Color colour = "SELL".equals(text) ? UiTheme.RED : UiTheme.GREEN;
        return label(text, UiTheme.BASE_BOLD, colour, SwingConstants.LEFT);
    }

    public static JLabel statusCell(String text) {
        Color fg;
        Color bg;
        if ("Filled".equals(text)) {
            fg = UiTheme.GREEN;
            bg = UiTheme.GREEN_TINT;
        } else if ("Cancelled".equals(text)) {
            fg = UiTheme.RED;
            bg = UiTheme.RED_TINT;
        } else {
            fg = UiTheme.AMBER;
            bg = UiTheme.AMBER_TINT;
        }
        JLabel label = label(text, UiTheme.BASE, fg, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(bg);
        label.setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
        return label;
    }

    public static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(UiTheme.BASE);
        button.setMargin(new Insets(2, 10, 2, 10));
        button.setFocusable(false);
        return button;
    }

    public static JLabel statusDot(Color colour) {
        JLabel dot = new JLabel("●");
        dot.setForeground(colour);
        dot.setFont(UiTheme.BASE);
        return dot;
    }

    public static Color signColour(String text) {
        String t = text == null ? "" : text.trim();
        if (t.startsWith("+")) {
            return UiTheme.GREEN;
        }
        if (t.startsWith("-")) {
            return UiTheme.RED;
        }
        return UiTheme.TEXT_MUTED;
    }
}
