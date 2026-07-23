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
import javax.swing.SwingConstants;

public class OrderHistoryPanel extends TitledPanel {

    public OrderHistoryPanel() {
        super("Order & Trade History");
        setPreferredSize(new Dimension(1440, 206));

        getContent().add(buildFilterRow(), BorderLayout.NORTH);
        getContent().add(buildTable(), BorderLayout.CENTER);
    }

    private JPanel buildFilterRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filters.setOpaque(false);
        String[] names = {"All Orders", "Pending", "Filled", "Cancelled"};
        for (String name : names) {
            JButton button = new JButton(name);
            button.setFont(UiTheme.BASE);
            button.setMargin(new Insets(3, 9, 3, 9));
            filters.add(button);
        }

        JLabel persisted = new JLabel("Persisted — restored on relaunch");
        persisted.setFont(UiTheme.BASE_ITALIC);
        persisted.setForeground(UiTheme.TEXT_LABEL);

        row.add(filters, BorderLayout.WEST);
        row.add(persisted, BorderLayout.EAST);
        return row;
    }

    private JPanel buildTable() {
        JPanel grid = new JPanel(new GridLayout(0, 9, 8, 2));
        grid.setOpaque(false);
        for (String col : PlaceholderData.ORDER_HISTORY_COLUMNS) {
            grid.add(ViewComponents.header(col, SwingConstants.LEFT));
        }
        for (String[] r : PlaceholderData.ORDER_HISTORY) {
            grid.add(ViewComponents.cell(r[0], SwingConstants.LEFT));
            grid.add(ViewComponents.cell(r[1], SwingConstants.LEFT));
            grid.add(ViewComponents.sideCell(r[2]));
            grid.add(ViewComponents.cell(r[3], SwingConstants.LEFT));
            grid.add(ViewComponents.cell(r[4], SwingConstants.LEFT));
            grid.add(ViewComponents.cell(r[5], SwingConstants.LEFT));
            grid.add(ViewComponents.cell(r[6], SwingConstants.LEFT));
            grid.add(ViewComponents.statusCell(r[7]));
            grid.add(ViewComponents.cell(r[8], SwingConstants.LEFT));
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiTheme.PANEL_BG);
        wrapper.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_LIGHT));
        wrapper.add(grid, BorderLayout.NORTH);
        return wrapper;
    }
}
