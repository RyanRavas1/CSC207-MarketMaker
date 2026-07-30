package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.time.Instant;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marketmaker.entities.Quote;
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

    private static final List<Column<Quote>> COLUMNS = List.of(
            Column.of("Symbol", String.class, Quote::getTicker),
            new Column<>("Last", Double.class, Quote::getPrice, CellStyle.NUMBER),
            Column.of("Updated", Instant.class, Quote::getTimestamp));

    private final ListTableModel<Quote> model = new ListTableModel<>(COLUMNS);
    private final JLabel updated = new JLabel();

    public WatchlistPanel(ViewModel<List<Quote>> viewModel) {
        super("Watchlist");
        setPreferredSize(new Dimension(300, 593));

        getContent().add(buildEntryRow(), BorderLayout.NORTH);
        getContent().add(Tables.scroll(Tables.create(model)), BorderLayout.CENTER);
        getContent().add(buildFooter(), BorderLayout.SOUTH);

        viewModel.onState(quotes -> {
            model.setRows(quotes);
            updated.setText("Updated " + Format.time(Instant.now()));
        });
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

        updated.setFont(UiTheme.BASE);
        updated.setForeground(UiTheme.TEXT_MUTED);

        footer.add(left, BorderLayout.WEST);
        footer.add(updated, BorderLayout.EAST);
        return footer;
    }
}
