package com.marketmaker.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.marketmaker.interface_adapter.user_profile.ViewProfileState;
import com.marketmaker.interface_adapter.user_profile.ViewProfileViewModel;

/**
 * The strip across the top of the window: cash, buying power, equity, and the two numbers a
 * day trader actually watches — what today has made or lost, and what has been banked.
 *
 * <p>Reads the same view model as the profile panel rather than running its own use case:
 * both screens are showing one valuation of one account, so they should never disagree.
 */
public class AccountSummaryView extends JPanel implements PropertyChangeListener {
    private static final Color GAIN = new Color(0x2E, 0x7D, 0x32);
    private static final Color LOSS = new Color(0xC6, 0x28, 0x28);

    private final JLabel cashLabel = new JLabel();
    private final JLabel buyingPowerLabel = new JLabel();
    private final JLabel equityLabel = new JLabel();
    private final JLabel dailyPnLLabel = new JLabel();
    private final JLabel realizedPnLLabel = new JLabel();

    public AccountSummaryView(ViewProfileViewModel viewModel) {
        super(new FlowLayout(FlowLayout.LEFT, 18, 4));
        viewModel.addPropertyChangeListener(this);
        setBorder(BorderFactory.createEtchedBorder());

        add(cashLabel);
        add(buyingPowerLabel);
        add(equityLabel);
        add(dailyPnLLabel);
        add(realizedPnLLabel);
        show(new ViewProfileState());
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // Presenters fire from the background worker; touch Swing only on the EDT.
        ViewProfileState state = (ViewProfileState) event.getNewValue();
        SwingUtilities.invokeLater(() -> show(state));
    }

    private void show(ViewProfileState state) {
        if (!state.getError().isEmpty()) {
            cashLabel.setText("Cash —");
            buyingPowerLabel.setText("Buying power —");
            equityLabel.setText("Total equity —");
            paint(dailyPnLLabel, "Day P/L", "");
            paint(realizedPnLLabel, "Realized today", "");
            return;
        }

        cashLabel.setText("Cash " + orDash(state.getCashBalance()));
        buyingPowerLabel.setText("Buying power " + orDash(state.getBuyingPower()));
        equityLabel.setText("Total equity " + orDash(state.getTotalEquity()));
        paint(dailyPnLLabel, "Day P/L", state.getDailyPnL());
        paint(realizedPnLLabel, "Realized today", state.getRealizedPnLToday());
    }

    // Money.format writes a leading "-" for losses, which is the only signal needed to colour
    // the label — no need to parse the number back out of the string.
    private void paint(JLabel label, String caption, String amount) {
        label.setText(caption + " " + orDash(amount));
        label.setForeground(amount.startsWith("-") ? LOSS : GAIN);
    }

    private String orDash(String amount) {
        return amount.isEmpty() ? "—" : amount;
    }
}
