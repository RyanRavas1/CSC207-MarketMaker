package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.marketmaker.entities.Quote;
import com.marketmaker.interface_adapter.WatchlistController;
import com.marketmaker.interface_adapter.ChartController;
import com.marketmaker.interface_adapter.ViewModel;

/**
 * The tickers being tracked, at their latest quoted price.
 *
 * <p>Bound to {@link Quote} because that is what {@code receive_live_quotes} delivers.
 * A percent-change column is deliberately absent: no current response model carries a
 * previous close, and the watchlist contract itself is still contested between PR #7
 * and PR #9. Add the column once one of those lands and exposes it.
 */
public class WatchlistPanel extends TitledPanel {

    // A quote older than this is the last trade before the close, not a live price.
    private static final Duration STALE_AFTER = Duration.ofMinutes(2);

    private static final List<Column<Quote>> COLUMNS = List.of(
            Column.of("Symbol", String.class, Quote::getTicker),
            new Column<>("Last", Double.class, Quote::getPrice, CellStyle.NUMBER),
            Column.of("Updated", Instant.class, Quote::getTimestamp));

    private final ListTableModel<Quote> model = new ListTableModel<>(COLUMNS);
    private final JLabel updated = new JLabel();
    private final JLabel live = new JLabel("NO DATA");
    private final JLabel dot = ViewComponents.statusDot(UiTheme.TEXT_MUTED);
    private final JTable table = Tables.create(model, "Watchlist quotes");
    private final JTextField symbolField = new JTextField();
    private final WatchlistController controller;
    private String charted;

    public WatchlistPanel(ViewModel<List<Quote>> viewModel, WatchlistController controller,
                          ChartController chartController) {
        super("Watchlist");
        this.controller = controller;
        setPreferredSize(new Dimension(300, 593));

        // Picking a row charts it. Only on a real change of ticker: the table is rebuilt on
        // every refresh, and re-charting the same symbol every ten seconds would spend the
        // day's Alpha Vantage quota on a line that hasn't moved.
        table.getSelectionModel().addListSelectionListener(event -> {
            String picked = selectedTicker();
            if (!event.getValueIsAdjusting() && picked != null && !picked.equals(charted)) {
                charted = picked;
                chartController.show(picked);
            }
        });

        getContent().add(buildEntryRow(), BorderLayout.NORTH);
        getContent().add(Tables.scroll(table), BorderLayout.CENTER);
        getContent().add(buildFooter(), BorderLayout.SOUTH);

        viewModel.onError(this::showProblem);
        viewModel.onState(quotes -> {
            model.setRows(quotes);
            showFreshness(quotes);
        });
    }

    /**
     * The status line reports the market's own timestamp, not the moment we asked. After the
     * close those are hours apart, and a green LIVE over a stale closing price is a lie the
     * user cannot see through.
     */
    private void showFreshness(List<Quote> quotes) {
        if (quotes == null || quotes.isEmpty()) {
            paintStatus(UiTheme.TEXT_MUTED, "NO DATA");
            updated.setText("");
            return;
        }

        Instant traded = quotes.get(0).getTimestamp();
        updated.setText("As of " + Format.time(traded));
        updated.setForeground(UiTheme.TEXT_LABEL);
        if (Duration.between(traded, Instant.now()).compareTo(STALE_AFTER) > 0) {
            paintStatus(UiTheme.TEXT_MUTED, "DELAYED");
        } else {
            paintStatus(UiTheme.GREEN, "LIVE");
        }
    }

    /** A rejected symbol or a ticker the feed had no price for, in the freshness line. */
    private void showProblem(String problem) {
        updated.setText(problem);
        updated.setForeground(UiTheme.RED);
    }

    private void paintStatus(Color colour, String text) {
        live.setText(text);
        live.setForeground(colour);
        dot.setForeground(colour);
    }

    /** The ticker on the highlighted row, or null when nothing is selected. */
    private String selectedTicker() {
        int row = table.getSelectedRow();
        return row < 0 ? null : model.getRow(table.convertRowIndexToModel(row)).getTicker();
    }

    private JPanel buildEntryRow() {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        symbolField.setFont(UiTheme.BASE);

        JButton add = smallButton("Add");
        add.setMnemonic(KeyEvent.VK_A);
        add.setToolTipText("Add the typed symbol to the watchlist (Alt+A)");
        add.addActionListener(event -> {
            controller.add(symbolField.getText());
            symbolField.setText("");
        });

        // Removing goes by the typed symbol when there is one, so a ticker that never got a
        // row — because it could not be quoted — can still be taken off the list.
        JButton remove = smallButton("Remove");
        remove.setMnemonic(KeyEvent.VK_M);
        remove.setToolTipText("Stop watching the selected symbol (Alt+M)");
        remove.addActionListener(event -> {
            String typed = symbolField.getText().trim();
            controller.remove(typed.isEmpty() ? selectedTicker() : typed);
            symbolField.setText("");
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttons.setOpaque(false);
        buttons.add(add);
        buttons.add(remove);

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

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        left.setOpaque(false);
        left.add(dot);
        live.setFont(UiTheme.BASE_BOLD);
        live.setForeground(UiTheme.TEXT_MUTED);
        left.add(live);

        updated.setFont(UiTheme.BASE);
        updated.setForeground(UiTheme.TEXT_MUTED);

        footer.add(left, BorderLayout.WEST);
        footer.add(updated, BorderLayout.EAST);
        return footer;
    }
}
