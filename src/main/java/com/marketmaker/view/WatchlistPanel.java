package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class WatchlistPanel extends TitledPanel {

    public WatchlistPanel() {
        super("Watchlist");
        setPreferredSize(new Dimension(300, 593));

        getContent().add(buildEntryRow(), BorderLayout.NORTH);
        getContent().add(buildTable(), BorderLayout.CENTER);
        getContent().add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildEntryRow() {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JTextField symbolField = new JTextField();
        symbolField.setFont(UiTheme.BASE);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttons.setOpaque(false);
        buttons.add(smallButton("Add"));
        buttons.add(smallButton("Remove"));

        row.add(symbolField, BorderLayout.CENTER);
        row.add(buttons, BorderLayout.EAST);
        return row;
    }

    private JButton smallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UiTheme.BASE);
        button.setMargin(new Insets(2, 8, 2, 8));
        return button;
    }

    private JPanel buildTable() {
        int[] align = {SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.RIGHT};
        JPanel grid = new JPanel(new GridLayout(0, 3, 8, 2));
        grid.setOpaque(false);
        for (int i = 0; i < PlaceholderData.WATCHLIST_COLUMNS.length; i++) {
            grid.add(ViewComponents.header(PlaceholderData.WATCHLIST_COLUMNS[i], align[i]));
        }
        for (String[] r : PlaceholderData.WATCHLIST) {
            grid.add(ViewComponents.cell(r[0], SwingConstants.LEFT));
            grid.add(ViewComponents.cell(r[1], SwingConstants.RIGHT));
            grid.add(ViewComponents.signedCell(r[2], SwingConstants.RIGHT));
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiTheme.PANEL_BG);
        wrapper.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        wrapper.add(grid, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        left.setOpaque(false);
        left.add(ViewComponents.statusDot(UiTheme.GREEN));
        JLabel live = new JLabel("LIVE");
        live.setFont(UiTheme.BASE_BOLD);
        live.setForeground(UiTheme.GREEN);
        left.add(live);

        JLabel updated = new JLabel("Updated 14:32:07 ET");
        updated.setFont(UiTheme.BASE);
        updated.setForeground(UiTheme.TEXT_MUTED);

        footer.add(left, BorderLayout.WEST);
        footer.add(updated, BorderLayout.EAST);
        return footer;
    }
}
