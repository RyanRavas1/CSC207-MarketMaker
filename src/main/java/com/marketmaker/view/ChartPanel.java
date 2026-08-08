package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.Instant;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.marketmaker.entities.Candle;
import com.marketmaker.interface_adapter.ChartController;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_candlestick_chart.Resolution;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartResponseModel;

/**
 * Price history for the selected ticker.
 *
 * <p>The bars are shown as a table rather than drawn candles; rendering the actual
 * candlestick chart needs custom painting and is left for the charting task. The
 * interval buttons are inert until a {@code ViewCandlestickChartInputBoundary}
 * controller exists to re-run the use case at a new {@code Resolution}.
 */
public class ChartPanel extends TitledPanel {

    private static final List<Column<Candle>> COLUMNS = List.of(
            Column.of("Time", Instant.class, Candle::getTimestamp),
            new Column<>("Open", Double.class, Candle::getOpen, CellStyle.NUMBER),
            new Column<>("High", Double.class, Candle::getHigh, CellStyle.NUMBER),
            new Column<>("Low", Double.class, Candle::getLow, CellStyle.NUMBER),
            new Column<>("Close", Double.class, Candle::getClose, CellStyle.NUMBER));

    private final ListTableModel<Candle> model = new ListTableModel<>(COLUMNS);

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
    private final JLabel source = ViewComponents.label("SAMPLE DATA — not a real price series",
            UiTheme.BASE_BOLD, UiTheme.RED, SwingConstants.LEFT);

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
        area.add(Tables.scroll(Tables.create(model)), BorderLayout.CENTER);

        getContent().add(header, BorderLayout.NORTH);
        getContent().add(area, BorderLayout.CENTER);

        viewModel.onState(this::show);
    }

    private void show(ViewCandlestickChartResponseModel response) {
        model.setRows(response.getCandles());
        ticker.setText(response.getTicker());

        List<Candle> candles = response.getCandles();
        if (candles == null || candles.isEmpty()) {
            return;
        }
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

    private JComponent buildQuoteRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 4, 10, 0));

        JPanel quote = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quote.setOpaque(false);
        quote.add(ticker);
        quote.add(last);
        quote.add(change);
        // Says on its face where these bars come from. Finnhub's free tier refuses
        // /stock/candle, so until a historical provider is settled these are generated —
        // and generated prices next to a live watchlist are indistinguishable otherwise.
        quote.add(source);

        row.add(quote, BorderLayout.WEST);
        row.add(buildIntervalPicker(), BorderLayout.EAST);
        return row;
    }

    private JComponent buildIntervalPicker() {
        JPanel picker = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        picker.setOpaque(false);
        picker.add(ViewComponents.caption("Range:"));
        picker.add(intervalButton("1W", Resolution.ONE_WEEK));
        picker.add(intervalButton("1M", Resolution.ONE_MONTH));
        return picker;
    }

    private JButton intervalButton(String text, Resolution resolution) {
        JButton button = ViewComponents.button(text);
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
