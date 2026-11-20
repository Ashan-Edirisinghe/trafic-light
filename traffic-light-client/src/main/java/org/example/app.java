package org.example;

import org.example.ui.MainFrame;

import javax.swing.*;

public class app {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use default look and feel
            }
            new MainFrame().setVisible(true);
        });
    }
}