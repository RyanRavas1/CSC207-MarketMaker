package com.marketmaker.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.marketmaker.interface_adapter.ProfileController;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.user_profile.ViewProfileResponseModel;

/**
 * The account at a glance: who it belongs to, what is in it, what it is worth.
 *
 * <p>A dialog rather than a panel because it repeats what the dashboard already shows in
 * pieces — it is worth having on demand, not worth permanent screen space.
 */
public class ProfileDialog extends JDialog {

    private static final List<Column<ViewProfileResponseModel.Holding>> COLUMNS = List.of(
            Column.of("Symbol", String.class, ViewProfileResponseModel.Holding::getTicker),
            new Column<>("Qty", Integer.class, ViewProfileResponseModel.Holding::getShares, CellStyle.NUMBER),
            new Column<>("Avg", Double.class, ViewProfileResponseModel.Holding::getAveragePrice, CellStyle.NUMBER),
            new Column<>("Last", Double.class, ViewProfileResponseModel.Holding::getCurrentPrice, CellStyle.NUMBER),
            new Column<>("Value", Double.class, ViewProfileResponseModel.Holding::getMarketValue, CellStyle.NUMBER),
            new Column<>("Unrl P/L", Double.class,
                    ViewProfileResponseModel.Holding::getUnrealizedPnL, CellStyle.SIGNED));

    private final ListTableModel<ViewProfileResponseModel.Holding> model = new ListTableModel<>(COLUMNS);
    private final JLabel name = value();
    private final JLabel cash = value();
    private final JLabel equity = value();
    private final JLabel message = new JLabel(" ");
    private final ProfileController controller;

    public ProfileDialog(Frame owner, ViewModel<ViewProfileResponseModel> viewModel,
                         ProfileController controller) {
        super(owner, "Account Profile", false);
        setSize(660, 340);
        setLocationRelativeTo(owner);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 8));
        header.setOpaque(false);
        addField(header, "Account", name);
        addField(header, "Cash", cash);
        addField(header, "Total equity", equity);

        message.setFont(UiTheme.BASE_ITALIC);
        message.setForeground(UiTheme.RED);
        message.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 0));

        JButton close = ViewComponents.button("Close");
        close.addActionListener(event -> setVisible(false));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        footer.setOpaque(false);
        footer.add(close);

        JPanel body = new JPanel(new BorderLayout());
        body.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        body.add(header, BorderLayout.NORTH);
        body.add(Tables.scroll(Tables.create(model, "Account holdings")), BorderLayout.CENTER);
        body.add(message, BorderLayout.SOUTH);

        add(body, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        viewModel.onState(this::show);
        viewModel.onError(message::setText);
        this.controller = controller;
    }

    /** Opens on fresh figures, so it never shows the balance from the last time it was open. */
    public void open() {
        controller.show();
        setVisible(true);
        toFront();
    }

    private void show(ViewProfileResponseModel profile) {
        name.setText(profile.getUserName());
        cash.setText("$" + Format.money(profile.getCashBalance()));
        equity.setText("$" + Format.money(profile.getTotalEquity()));
        model.setRows(profile.getHoldings());
        message.setText(" ");
    }

    private static void addField(JPanel row, String label, JLabel amount) {
        JPanel field = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        field.setOpaque(false);
        field.add(ViewComponents.label(label, UiTheme.BASE, UiTheme.TEXT_LABEL, SwingConstants.LEFT));
        field.add(amount);
        field.setPreferredSize(new Dimension(185, 20));
        row.add(field);
    }

    private static JLabel value() {
        return ViewComponents.label(Format.ABSENT, UiTheme.BASE_BOLD, UiTheme.TEXT, SwingConstants.LEFT);
    }
}
