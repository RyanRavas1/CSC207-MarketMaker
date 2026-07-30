package com.marketmaker.view;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;

/** The account strip under the toolbar: cash, buying power, equity and the day's P/L. */
public class AccountSummaryBar extends JPanel {

    private final JLabel cash = value(UiTheme.TEXT);
    private final JLabel buyingPower = value(UiTheme.TEXT);
    private final JLabel equity = value(UiTheme.TEXT);
    private final JLabel dayPl = value(UiTheme.GREEN);

    public AccountSummaryBar(ViewModel<ViewPortfolioSummaryResponseModel> viewModel) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBackground(UiTheme.BAR_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(11, 10, 11, 10)));
        setPreferredSize(new Dimension(1440, 38));

        addStat("Cash", cash);
        addDivider();
        addStat("Buying Power", buyingPower);
        addDivider();
        addStat("Equity", equity);
        addDivider();
        addStat("Day P/L", dayPl);

        viewModel.onState(this::show);
    }

    private void show(ViewPortfolioSummaryResponseModel summary) {
        cash.setText("$" + Format.money(summary.getCash()));
        buyingPower.setText("$" + Format.money(summary.getBuyingPower()));
        equity.setText("$" + Format.money(summary.getTotalEquity()));
        dayPl.setText(Format.signedDollars(summary.getDailyPnL()));
        dayPl.setForeground(summary.getDailyPnL() < 0 ? UiTheme.RED : UiTheme.GREEN);
    }

    private static JLabel value(java.awt.Color colour) {
        JLabel label = new JLabel(Format.ABSENT);
        label.setFont(UiTheme.BASE_BOLD);
        label.setForeground(colour);
        return label;
    }

    private void addStat(String caption, JLabel amount) {
        JLabel label = new JLabel(caption);
        label.setFont(UiTheme.BASE);
        label.setForeground(UiTheme.TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
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
