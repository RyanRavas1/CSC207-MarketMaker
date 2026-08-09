package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marketmaker.entities.Order;
import com.marketmaker.interface_adapter.OrderTicketController;
import com.marketmaker.interface_adapter.RealizedPnLController;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;

public class OrderTicketPanel extends TitledPanel {

    private final JLabel buyingPower = new JLabel(Format.ABSENT);
    private final RealizedPnLController realizedPnL;
    private final JLabel estimatedCost = new JLabel(Format.ABSENT);
    private final JLabel hint = new JLabel(" ");

    private final JTextField symbolField = textField("", true);
    private final JTextField quantityField = textField("10", true);
    private final JTextField limitField = textField("", true);
    private final JTextField stopField = textField("", false);

    private final JRadioButton buy = radio("Buy", true);
    private final JRadioButton sell = radio("Sell", false);
    private final JRadioButton market = radio("Market", true);
    private final JRadioButton limit = radio("Limit", false);
    private final JRadioButton stop = radio("Stop", false);

    private final JButton place = placeButton();
    private final OrderTicketController controller;

    private double availableCash;

    public OrderTicketPanel(ViewModel<ViewPortfolioSummaryResponseModel> summary,
                            ViewModel<String> status, OrderTicketController controller,
                            RealizedPnLController realizedPnL) {
        super("Order Ticket");
        this.controller = controller;
        this.realizedPnL = realizedPnL;
        setPreferredSize(new Dimension(358, 450));

        getContent().add(buildForm(), BorderLayout.NORTH);
        getContent().add(buildSubmitGroup(), BorderLayout.SOUTH);

        summary.onState(response -> {
            availableCash = response.getBuyingPower();
            buyingPower.setText("$" + Format.money(response.getBuyingPower()));
            refreshEstimate();
        });
        status.onState(hint::setText);

        wire();
    }

    /** Keeps the form honest about itself: the button, the enabled fields and the estimate. */
    private void wire() {
        ActionListener onChange = event -> refreshEstimate();
        for (JRadioButton button : List.of(buy, sell, market, limit, stop)) {
            button.addActionListener(onChange);
        }
        DocumentListener typing = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) { refreshEstimate(); }

            @Override
            public void removeUpdate(DocumentEvent event) { refreshEstimate(); }

            @Override
            public void changedUpdate(DocumentEvent event) { refreshEstimate(); }
        };
        quantityField.getDocument().addDocumentListener(typing);
        limitField.getDocument().addDocumentListener(typing);

        place.addActionListener(event -> submit());
        refreshEstimate();
    }

    private void submit() {
        Order.Side side = buy.isSelected() ? Order.Side.BUY : Order.Side.SELL;
        Order.Type type = orderType();
        String trigger = type == Order.Type.STOP_LOSS ? stopField.getText() : limitField.getText();

        String problem = controller.place(symbolField.getText(), side, type,
                quantityField.getText(), trigger);
        if (problem != null) {
            hint.setText(problem);
            hint.setForeground(UiTheme.RED);
        }
    }

    /** Lets the toolbar's Buy and Sell put the ticket on the right side of the trade. */
    public void chooseSide(boolean buying) {
        buy.setSelected(buying);
        sell.setSelected(!buying);
        refreshEstimate();
        symbolField.requestFocusInWindow();
    }

    private Order.Type orderType() {
        if (market.isSelected()) {
            return Order.Type.MARKET;
        }
        return limit.isSelected() ? Order.Type.LIMIT : Order.Type.STOP_LOSS;
    }

    /**
     * A market order has no price to quote against until it fills, so the estimate only
     * appears once the user names a limit or stop price.
     */
    private void refreshEstimate() {
        place.setText(buy.isSelected() ? "Place Buy Order" : "Place Sell Order");
        limitField.setEnabled(limit.isSelected());
        stopField.setEnabled(stop.isSelected());

        Double price = parse(stop.isSelected() ? stopField.getText() : limitField.getText());
        Integer quantity = parseWhole(quantityField.getText());
        if (price == null || quantity == null || market.isSelected()) {
            estimatedCost.setText(Format.ABSENT);
            hint.setText(" ");
            return;
        }

        double cost = price * quantity;
        estimatedCost.setText("$" + Format.money(cost));
        if (buy.isSelected() && cost > availableCash) {
            hint.setText("Not enough buying power");
            hint.setForeground(UiTheme.RED);
        } else if (buy.isSelected()) {
            hint.setText("Sufficient buying power");
            hint.setForeground(UiTheme.GREEN);
        } else {
            showRealized(price, quantity);
        }
    }

    /**
     * A sell has no buying-power problem to warn about, so the line is spent on what the sale
     * would actually realize against the average price paid — the number a seller is weighing.
     */
    private void showRealized(double price, int quantity) {
        String realized = realizedPnL.estimate(symbolField.getText(), quantity, price);
        hint.setText(realized == null ? " " : realized);
        hint.setForeground(UiTheme.TEXT_MUTED);
    }

    private Double parse(String text) {
        try {
            return Double.valueOf(text.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer parseWhole(String text) {
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private JComponent buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        group(buy, sell);
        group(market, limit, stop);

        // No Time in Force row: an Order has no expiry, so every order behaves as Day. Three
        // permanently greyed-out radio buttons read as a broken feature; leaving them out
        // reads as a feature that isn't offered.

        form.add(labelledRow("Symbol", symbolField));
        form.add(labelledRow("Side", radioRow(buy, sell)));
        form.add(labelledRow("Order Type", radioRow(market, limit, stop)));
        form.add(labelledRow("Quantity", quantityField));
        form.add(labelledRow("Limit Price", limitField));
        form.add(labelledRow("Stop Price", stopField));
        return form;
    }

    private JComponent buildSubmitGroup() {
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        bottom.add(divider());
        estimatedCost.setFont(UiTheme.BASE_BOLD);
        estimatedCost.setForeground(UiTheme.TEXT);
        bottom.add(summaryRow("Estimated cost", estimatedCost));
        buyingPower.setFont(UiTheme.BASE);
        buyingPower.setForeground(UiTheme.TEXT);
        bottom.add(summaryRow("Buying power", buyingPower));

        hint.setFont(UiTheme.BASE);
        hint.setForeground(UiTheme.GREEN);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        bottom.add(hint);

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
        // Binds the caption to the field it describes. Without this a screen reader
        // announces the field as an unnamed text box, however close the label sits.
        label.setLabelFor(control);

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

    /** The ticket's primary action, reachable as Alt+P. */
    private static JButton placeButton() {
        JButton button = new JButton("Place Buy Order");
        button.setMnemonic(KeyEvent.VK_P);
        button.setToolTipText("Submit this order ticket (Alt+P)");
        return button;
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
