package com.marketmaker.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JComponent;

/**
 * Draws a price series as a single line.
 *
 * <p>Takes plain numbers rather than candles: a line chart only needs one price per point,
 * and keeping the entity out means the drawing code doesn't move when the data source does.
 * A candlestick chart would need the other three prices, which is the upgrade path if the
 * team ever wants one.
 */
public class PriceLineChart extends JComponent {

    private static final int PADDING_LEFT = 8;
    private static final int PADDING_RIGHT = 62;
    private static final int PADDING_TOP = 10;
    private static final int PADDING_BOTTOM = 22;
    private static final int GRID_LINES = 4;
    // Roughly a label a week over a month, and one a day over a week. More than this and the
    // dates start touching at the panel's usual width.
    private static final int MAX_DATE_LABELS = 6;
    private static final int LABEL_GAP = 8;

    private List<Double> prices = List.of();
    private List<String> dates = List.of();

    /** @param dates one date per price, in the same order */
    public void setSeries(List<Double> prices, List<String> dates) {
        this.prices = prices == null ? List.of() : prices;
        this.dates = dates == null ? List.of() : dates;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int plotWidth = getWidth() - PADDING_LEFT - PADDING_RIGHT;
        int plotHeight = getHeight() - PADDING_TOP - PADDING_BOTTOM;
        if (prices.size() < 2 || plotWidth <= 0 || plotHeight <= 0) {
            drawEmpty(canvas);
            canvas.dispose();
            return;
        }

        double low = prices.stream().min(Double::compare).orElse(0.0);
        double high = prices.stream().max(Double::compare).orElse(0.0);
        // A flat series would divide by zero, so give it a band to sit in the middle of.
        if (high - low < 0.01) {
            high += 0.5;
            low -= 0.5;
        }

        drawGrid(canvas, plotWidth, plotHeight, low, high);
        drawLine(canvas, plotWidth, plotHeight, low, high);
        drawDates(canvas, plotWidth, plotHeight);
        canvas.dispose();
    }

    private void drawGrid(Graphics2D canvas, int plotWidth, int plotHeight, double low, double high) {
        canvas.setFont(UiTheme.BASE);
        for (int line = 0; line <= GRID_LINES; line++) {
            int y = PADDING_TOP + plotHeight * line / GRID_LINES;
            canvas.setColor(UiTheme.BORDER_LIGHT);
            canvas.drawLine(PADDING_LEFT, y, PADDING_LEFT + plotWidth, y);

            // Top gridline is the high, so the fraction counts downward.
            double price = high - (high - low) * line / GRID_LINES;
            canvas.setColor(UiTheme.TEXT_LABEL);
            canvas.drawString(Format.money(price), PADDING_LEFT + plotWidth + 6, y + 4);
        }
    }

    private void drawLine(Graphics2D canvas, int plotWidth, int plotHeight, double low, double high) {
        int points = prices.size();
        int[] xs = new int[points];
        int[] ys = new int[points];
        for (int i = 0; i < points; i++) {
            xs[i] = PADDING_LEFT + plotWidth * i / (points - 1);
            // Screen y grows downward while price grows upward, hence the subtraction.
            ys[i] = PADDING_TOP + (int) (plotHeight * (high - prices.get(i)) / (high - low));
        }

        Color colour = prices.get(points - 1) < prices.get(0) ? UiTheme.RED : UiTheme.GREEN;
        canvas.setColor(colour);
        canvas.setStroke(new BasicStroke(1.6f));
        canvas.drawPolyline(xs, ys, points);
    }

    /**
     * Dates along the bottom, thinned to fit and always including the last one — the right
     * edge is today, and a chart whose final tick is three days ago reads as out of date.
     */
    private void drawDates(Graphics2D canvas, int plotWidth, int plotHeight) {
        if (dates.size() != prices.size()) {
            return;
        }

        canvas.setColor(UiTheme.TEXT_LABEL);
        canvas.setFont(UiTheme.BASE);
        int points = dates.size();
        int step = Math.max(1, (int) Math.ceil((double) points / MAX_DATE_LABELS));
        int baseline = getHeight() - 6;
        // Labels are drawn right to left, so this is the left edge of the one before it.
        int previousLeft = Integer.MAX_VALUE;

        // Walked backwards so the last bar is labelled and the thinning falls on older ones.
        for (int i = points - 1; i >= 0; i -= step) {
            String date = dates.get(i);
            int width = canvas.getFontMetrics().stringWidth(date);
            int centre = PADDING_LEFT + plotWidth * i / (points - 1);
            int left = Math.min(Math.max(centre - width / 2, PADDING_LEFT),
                    PADDING_LEFT + plotWidth - width);
            if (left + width + LABEL_GAP > previousLeft) {
                continue;
            }
            canvas.drawString(date, left, baseline);
            canvas.setColor(UiTheme.BORDER_LIGHT);
            canvas.drawLine(centre, PADDING_TOP + plotHeight, centre, PADDING_TOP + plotHeight + 3);
            canvas.setColor(UiTheme.TEXT_LABEL);
            previousLeft = left;
        }
    }

    private void drawEmpty(Graphics2D canvas) {
        canvas.setColor(UiTheme.TEXT_LABEL);
        canvas.setFont(UiTheme.BASE);
        canvas.drawString("No price history to chart.", PADDING_LEFT, getHeight() / 2);
    }
}
