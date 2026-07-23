package com.marketmaker.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AccountSummaryBar extends JPanel {

    public AccountSummaryBar() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBackground(UiTheme.BAR_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(11, 10, 11, 10)));
        setPreferredSize(new Dimension(1440, 38));

        addStat("Starting", PlaceholderData.STARTING, UiTheme.TEXT);
        addDivider();
        addStat("Cash", PlaceholderData.CASH, UiTheme.TEXT);
        addDivider();
        addStat("Buying Power", PlaceholderData.BUYING_POWER, UiTheme.TEXT);
        addDivider();
        addStat("Equity", PlaceholderData.EQUITY, UiTheme.TEXT);
        addDivider();
        addStat("Day P/L", PlaceholderData.DAY_PL, UiTheme.GREEN);
    }

    private void addStat(String caption, String value, Color valueColor) {
        JLabel label = new JLabel(caption);
        label.setFont(UiTheme.BASE);
        label.setForeground(UiTheme.TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        JLabel amount = new JLabel(value);
        amount.setFont(UiTheme.BASE_BOLD);
        amount.setForeground(valueColor);

        add(label);
        add(amount);
    }

    private void addDivider() {
        JLabel divider = new JLabel("|");
        divider.setForeground(UiTheme.BORDER);
        divider.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(divider);
    }
}
