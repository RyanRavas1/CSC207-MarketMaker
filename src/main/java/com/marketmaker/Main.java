package com.marketmaker;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.marketmaker.view.DashboardFrame;

public class Main {

    public static void main(String[] args) {
        // Use the cross-platform look so it renders the same on everyone's machine.
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new DashboardFrame().setVisible(true));
    }
}
