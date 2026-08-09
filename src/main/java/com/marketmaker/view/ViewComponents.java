package com.marketmaker.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

// Helpers for making the labels and buttons the panels are built from, so the
// styling doesn't get repeated everywhere.
public final class ViewComponents {

    private ViewComponents() {
    }


    public static JLabel label(String text, Font font, Color colour, int align) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(colour);
        label.setHorizontalAlignment(align);
        return label;
    }

    public static JLabel caption(String text) {
        return label(text, UiTheme.BASE, UiTheme.TEXT_MUTED, SwingConstants.LEFT);
    }

    public static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(UiTheme.BASE);
        button.setMargin(new Insets(2, 10, 2, 10));
        // Focusable on purpose: setFocusable(false) took every button out of the Tab
        // order, which left the dashboard unusable without a mouse.
        button.setFocusable(true);
        return button;
    }

    public static JLabel statusDot(Color colour) {
        JLabel dot = new JLabel("●");
        dot.setForeground(colour);
        dot.setFont(UiTheme.BASE);
        return dot;
    }
}
