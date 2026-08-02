package com.marketmaker.view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

// A bordered panel with a small title in the top-left. Put content in getContent().
public class TitledPanel extends JPanel {

    private final JPanel content = new JPanel(new BorderLayout());

    public TitledPanel(String title) {
        super(new BorderLayout());
        setBackground(UiTheme.PANEL_BG);
        setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));

        JLabel caption = new JLabel(title);
        caption.setFont(UiTheme.BASE_BOLD);
        caption.setForeground(UiTheme.TEXT);
        caption.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));

        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        add(caption, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public JPanel getContent() {
        return content;
    }
}
