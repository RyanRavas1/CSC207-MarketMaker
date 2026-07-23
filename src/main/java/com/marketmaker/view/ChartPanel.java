package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// The real candlestick chart would need custom drawing, so for now we just show
// the recent price bars in a small table.
public class ChartPanel extends TitledPanel {

    public ChartPanel() {
        super("Chart");

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(buildQuoteRow(), BorderLayout.NORTH);
        header.add(buildOhlcRow(), BorderLayout.SOUTH);

        getContent().add(header, BorderLayout.NORTH);
        getContent().add(buildChartArea(), BorderLayout.CENTER);
    }

    private JComponent buildQuoteRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 4, 10, 0));

        JPanel quote = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quote.setOpaque(false);
        quote.add(ViewComponents.label(PlaceholderData.SYMBOL, UiTheme.TICKER, UiTheme.TEXT, SwingConstants.LEFT));
        quote.add(ViewComponents.label(PlaceholderData.COMPANY, UiTheme.BASE, UiTheme.TEXT_MUTED, SwingConstants.LEFT));
        quote.add(ViewComponents.label(PlaceholderData.LAST, UiTheme.PRICE, UiTheme.TEXT, SwingConstants.LEFT));
        quote.add(ViewComponents.signedCell(PlaceholderData.CHANGE, SwingConstants.LEFT));

        row.add(quote, BorderLayout.WEST);
        row.add(buildIntervalPicker(), BorderLayout.EAST);
        return row;
    }

    private JComponent buildIntervalPicker() {
        JPanel picker = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        picker.setOpaque(false);
        picker.add(ViewComponents.caption("Interval:"));
        picker.add(ViewComponents.button("1m"));
        picker.add(ViewComponents.button("5m"));
        picker.add(ViewComponents.button("1D"));
        return picker;
    }

    private JComponent buildOhlcRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 0));
        addStat(row, "O", PlaceholderData.OPEN);
        addStat(row, "H", PlaceholderData.HIGH);
        addStat(row, "L", PlaceholderData.LOW);
        addStat(row, "C", PlaceholderData.CLOSE);
        addStat(row, "Vol", PlaceholderData.VOLUME);
        return row;
    }

    private void addStat(JPanel row, String name, String value) {
        JLabel key = ViewComponents.label(name, UiTheme.BASE, UiTheme.TEXT_LABEL, SwingConstants.LEFT);
        key.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        JLabel amount = ViewComponents.label(value, UiTheme.BASE_BOLD, UiTheme.TEXT, SwingConstants.LEFT);
        amount.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
        row.add(key);
        row.add(amount);
    }

    private JComponent buildChartArea() {
        JPanel area = new JPanel(new BorderLayout());
        area.setBackground(UiTheme.PANEL_BG);
        area.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_LIGHT));
        area.setPreferredSize(new Dimension(730, 430));

        JLabel caption = ViewComponents.label("Recent bars", UiTheme.BASE_BOLD, UiTheme.TEXT, SwingConstants.LEFT);
        caption.setBorder(BorderFactory.createEmptyBorder(8, 8, 6, 8));

        int[] align = {SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.RIGHT,
                SwingConstants.RIGHT, SwingConstants.RIGHT};
        JPanel grid = new JPanel(new GridLayout(0, 5, 8, 2));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        for (int i = 0; i < PlaceholderData.RECENT_BARS_COLUMNS.length; i++) {
            grid.add(ViewComponents.header(PlaceholderData.RECENT_BARS_COLUMNS[i], align[i]));
        }
        for (String[] b : PlaceholderData.RECENT_BARS) {
            grid.add(ViewComponents.cell(b[0], SwingConstants.LEFT));
            grid.add(ViewComponents.cell(b[1], SwingConstants.RIGHT));
            grid.add(ViewComponents.cell(b[2], SwingConstants.RIGHT));
            grid.add(ViewComponents.cell(b[3], SwingConstants.RIGHT));
            grid.add(ViewComponents.cell(b[4], SwingConstants.RIGHT));
        }

        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setOpaque(false);
        gridWrap.add(grid, BorderLayout.NORTH);

        area.add(caption, BorderLayout.NORTH);
        area.add(gridWrap, BorderLayout.CENTER);
        return area;
    }
}
