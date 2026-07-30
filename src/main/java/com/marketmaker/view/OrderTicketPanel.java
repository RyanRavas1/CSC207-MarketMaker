package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;

public class OrderTicketPanel extends TitledPanel {

    private final JLabel buyingPower = new JLabel(Format.ABSENT);

    public OrderTicketPanel(ViewModel<ViewPortfolioSummaryResponseModel> summary) {
        super("Order Ticket");
        setPreferredSize(new Dimension(358, 450));

        getContent().add(buildForm(), BorderLayout.NORTH);
        getContent().add(buildSubmitGroup(), BorderLayout.SOUTH);

        summary.onState(response -> buyingPower.setText("$" + Format.money(response.getBuyingPower())));
    }

    private JComponent buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JRadioButton buy = radio("Buy", true);
        JRadioButton sell = radio("Sell", false);
        group(buy, sell);

        JRadioButton market = radio("Market", false);
        JRadioButton limit = radio("Limit", true);
        JRadioButton stop = radio("Stop", false);
        group(market, limit, stop);

        JRadioButton day = radio("Day", true);
        JRadioButton gtc = radio("GTC", false);
        JRadioButton ioc = radio("IOC", false);
        group(day, gtc, ioc);

        form.add(labelledRow("Side", radioRow(buy, sell)));
        form.add(labelledRow("Order Type", radioRow(market, limit, stop)));
        form.add(labelledRow("Quantity", textField("10", true)));
        form.add(labelledRow("Limit Price", textField("228.50", true)));
        form.add(labelledRow("Stop Price", textField("—", false)));
        form.add(labelledRow("Time in Force", radioRow(day, gtc, ioc)));
        return form;
    }

    private JComponent buildSubmitGroup() {
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        bottom.add(divider());
        bottom.add(summaryRow("Estimated cost", "$2,285.00", UiTheme.BASE_BOLD));
        buyingPower.setFont(UiTheme.BASE);
        buyingPower.setForeground(UiTheme.TEXT);
        bottom.add(summaryRow("Buying power", buyingPower));

        JLabel hint = new JLabel("Sufficient buying power");
        hint.setFont(UiTheme.BASE);
        hint.setForeground(UiTheme.GREEN);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        bottom.add(hint);

        JButton place = new JButton("Place Buy Order");
        place.setFont(UiTheme.BASE_BOLD);
        place.setAlignmentX(Component.LEFT_ALIGNMENT);
        place.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        bottom.add(place);
        return bottom;
    }

    private JComponent labelledRow(String caption, JComponent control) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel label = new JLabel(caption);
        label.setFont(UiTheme.BASE);
        label.setForeground(UiTheme.TEXT);
        label.setPreferredSize(new Dimension(90, 26));

        row.add(label, BorderLayout.WEST);
        row.add(control, BorderLayout.CENTER);
        return row;
    }

    private JComponent radioRow(JRadioButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        for (JRadioButton b : buttons) {
            row.add(b);
        }
        return row;
    }

    private JRadioButton radio(String text, boolean selected) {
        JRadioButton r = new JRadioButton(text, selected);
        r.setFont(UiTheme.BASE);
        r.setOpaque(false);
        return r;
    }

    private void group(JRadioButton... buttons) {
        ButtonGroup g = new ButtonGroup();
        for (JRadioButton b : buttons) {
            g.add(b);
        }
    }

    private JTextField textField(String text, boolean enabled) {
        JTextField field = new JTextField(text);
        field.setFont(UiTheme.BASE);
        field.setEnabled(enabled);
        if (!enabled) {
            field.setForeground(UiTheme.TEXT_LABEL);
        }
        return field;
    }

    private JComponent divider() {
        JPanel d = new JPanel();
        d.setBackground(UiTheme.BORDER_LIGHT);
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setPreferredSize(new Dimension(10, 1));
        return d;
    }

    private JComponent summaryRow(String caption, String value, Font valueFont) {
        JLabel right = new JLabel(value);
        right.setFont(valueFont);
        right.setForeground(UiTheme.TEXT);
        return summaryRow(caption, right);
    }

    private JComponent summaryRow(String caption, JLabel right) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel left = new JLabel(caption);
        left.setFont(UiTheme.BASE);
        left.setForeground(UiTheme.TEXT);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }
}
