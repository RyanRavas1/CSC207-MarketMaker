package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.marketmaker.entities.Candle;
import com.marketmaker.interface_adapter.ChartController;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_candlestick_chart.Resolution;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartResponseModel;

/**
 * Price history for the selected ticker.
 *
 * <p>Drawn as a line of closing prices rather than candlesticks: a free Alpha Vantage key
 * only sees one bar a day, and a day's open, high and low say little that its close doesn't.
 * The range buttons re-run the use case over a longer or shorter span.
 */
public class ChartPanel extends TitledPanel {

    private final PriceLineChart graph = new PriceLineChart();

    private final JLabel ticker = ViewComponents.label(Format.ABSENT, UiTheme.TICKER, UiTheme.TEXT,
            SwingConstants.LEFT);
    private final JLabel last = ViewComponents.label(Format.ABSENT, UiTheme.PRICE, UiTheme.TEXT,
            SwingConstants.LEFT);
    private final JLabel change = ViewComponents.label(Format.ABSENT, UiTheme.BASE_BOLD, UiTheme.TEXT_MUTED,
            SwingConstants.LEFT);
    private final JLabel open = statValue();
    private final JLabel high = statValue();
    private final JLabel low = statValue();
    private final JLabel close = statValue();
    private final JLabel volume = statValue();
    private static final String SOURCE_TEXT = "Daily closes — Alpha Vantage";

    private final JLabel source = ViewComponents.label(SOURCE_TEXT,
            UiTheme.BASE_ITALIC, UiTheme.TEXT_LABEL, SwingConstants.LEFT);
    // Charting is not limited to what is on the watchlist: a symbol can be looked at before
    // deciding to follow it. Enter rather than every keystroke, because each new symbol costs
    // an API call from a small daily allowance.
    private final JTextField symbolField = new JTextField(6);

    private final ChartController controller;

    public ChartPanel(ViewModel<ViewCandlestickChartResponseModel> viewModel, ChartController controller) {
        super("Chart");
        this.controller = controller;

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(buildQuoteRow(), BorderLayout.NORTH);
        header.add(buildOhlcRow(), BorderLayout.SOUTH);

        JPanel area = new JPanel(new BorderLayout());
        area.setOpaque(false);
        area.setPreferredSize(new Dimension(730, 430));
        area.add(graph, BorderLayout.CENTER);

        getContent().add(header, BorderLayout.NORTH);
        getContent().add(area, BorderLayout.CENTER);

        viewModel.onState(this::show);
        viewModel.onError(this::showProblem);
    }

    /**
     * Why the chart is empty, in the line that normally names the data source. A blank panel
     * invites the user to keep clicking; a reason tells them whether to wait or retype.
     */
    private void showProblem(String problem) {
        graph.setSeries(List.of(), List.of());
        source.setText(problem);
        source.setFont(UiTheme.BASE_BOLD);
        source.setForeground(UiTheme.AMBER);
        for (JLabel stat : List.of(last, change, open, high, low, close, volume)) {
            stat.setText(Format.ABSENT);
        }
    }

    private void show(ViewCandlestickChartResponseModel response) {
        ticker.setText(response.getTicker());
        source.setText(SOURCE_TEXT);
        source.setFont(UiTheme.BASE_ITALIC);
        source.setForeground(UiTheme.TEXT_LABEL);

        List<Candle> candles = response.getCandles();
        if (candles == null || candles.isEmpty()) {
            graph.setSeries(List.of(), List.of());
            return;
        }
        plot(candles);
        Candle latest = candles.get(candles.size() - 1);
        double first = candles.get(0).getOpen();
        double delta = latest.getClose() - first;

        last.setText(Format.money(latest.getClose()));
        change.setText(Format.signedMoney(delta)
                + " (" + Format.percent(first == 0 ? null : delta / first * 100) + ")");
        change.setForeground(delta < 0 ? UiTheme.RED : UiTheme.GREEN);

        open.setText(Format.money(latest.getOpen()));
        high.setText(Format.money(latest.getHigh()));
        low.setText(Format.money(latest.getLow()));
        close.setText(Format.money(latest.getClose()));
        volume.setText(Format.volume(latest.getVolume()));
    }

    private void plot(List<Candle> candles) {
        List<Double> closes = new ArrayList<>(candles.size());
        List<String> dates = new ArrayList<>(candles.size());
        for (Candle candle : candles) {
            closes.add(candle.getClose());
            dates.add(day(candle));
        }
        graph.setSeries(closes, dates);
    }

    /**
     * The bar's month and day. Daily bars all land at the same hour, so the time says nothing,
     * and the year is the same across every range the chart offers.
     */
    private static String day(Candle candle) {
        return candle.getTimestamp().toString().substring(5, 10);
    }

    private JComponent buildQuoteRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 4, 10, 0));

        JPanel quote = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quote.setOpaque(false);
        quote.add(ticker);
        quote.add(last);
        quote.add(change);
        // Says on its face where the line comes from: these are end-of-day closes, so they
        // will not agree with the live price beside them, and that difference is not a bug.
        quote.add(source);

        row.add(quote, BorderLayout.WEST);
        row.add(buildIntervalPicker(), BorderLayout.EAST);
        return row;
    }

    private JComponent buildIntervalPicker() {
        JPanel picker = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        picker.setOpaque(false);
        symbolField.setFont(UiTheme.BASE);
        symbolField.setToolTipText("Type a symbol and press Enter to chart it");
        symbolField.addActionListener(event -> {
            controller.show(symbolField.getText());
            symbolField.setText("");
        });
        picker.add(ViewComponents.caption("Symbol:"));
        picker.add(symbolField);
        picker.add(ViewComponents.caption("Range:"));
        picker.add(intervalButton("1W", Resolution.ONE_WEEK));
        picker.add(intervalButton("1M", Resolution.ONE_MONTH));
        return picker;
    }

    private JButton intervalButton(String text, Resolution resolution) {
        JButton button = ViewComponents.button(text);
        button.getAccessibleContext().setAccessibleName("Chart span " + text);
        button.setToolTipText("Show the last " + text.replace("1W", "week").replace("1M", "month"));
        button.addActionListener(event -> controller.showInterval(resolution));
        return button;
    }

    private JComponent buildOhlcRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 0));
        addStat(row, "O", open);
        addStat(row, "H", high);
        addStat(row, "L", low);
        addStat(row, "C", close);
        addStat(row, "Vol", volume);
        return row;
    }

    private void addStat(JPanel row, String name, JLabel amount) {
        JLabel key = ViewComponents.label(name, UiTheme.BASE, UiTheme.TEXT_LABEL, SwingConstants.LEFT);
        key.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        amount.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
        row.add(key);
        row.add(amount);
    }

    private static JLabel statValue() {
        return ViewComponents.label(Format.ABSENT, UiTheme.BASE_BOLD, UiTheme.TEXT, SwingConstants.LEFT);
    }
}
