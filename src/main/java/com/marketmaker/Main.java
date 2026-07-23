package com.marketmaker;

import com.marketmaker.data_access.HardcodedOrderHistoryDataAccessObject;
import com.marketmaker.use_case.order_history.OrderHistoryDataAccessInterface;
import com.marketmaker.view.OrderHistoryView;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OrderHistoryDataAccessInterface dataAccess = new HardcodedOrderHistoryDataAccessObject();
            JFrame frame = new JFrame("MarketMaker — Order History");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new OrderHistoryView(dataAccess));
            frame.setSize(800, 300);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
