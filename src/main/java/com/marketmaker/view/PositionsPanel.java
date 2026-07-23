package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PositionsPanel extends TitledPanel {

    public PositionsPanel() {
        super("Positions");
        setPreferredSize(new Dimension(358, 138));

        int[] align = {SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.RIGHT,
                SwingConstants.RIGHT, SwingConstants.RIGHT, SwingConstants.RIGHT};

        JPanel grid = new JPanel(new GridLayout(0, 6, 8, 2));
        grid.setOpaque(false);
        for (int i = 0; i < PlaceholderData.POSITIONS_COLUMNS.length; i++) {
            grid.add(ViewComponents.header(PlaceholderData.POSITIONS_COLUMNS[i], align[i]));
        }
        for (String[] r : PlaceholderData.POSITIONS) {
            grid.add(ViewComponents.cell(r[0], SwingConstants.LEFT));
            grid.add(ViewComponents.cell(r[1], SwingConstants.RIGHT));
            grid.add(ViewComponents.cell(r[2], SwingConstants.RIGHT));
            grid.add(ViewComponents.cell(r[3], SwingConstants.RIGHT));
            grid.add(ViewComponents.cell(r[4], SwingConstants.RIGHT));
            grid.add(ViewComponents.signedCell(r[5], SwingConstants.RIGHT));
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiTheme.PANEL_BG);
        wrapper.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_LIGHT));
        wrapper.add(grid, BorderLayout.NORTH);
        getContent().add(wrapper, BorderLayout.CENTER);
    }
}
