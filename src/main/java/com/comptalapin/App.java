package com.comptalapin;

import com.comptalapin.ui.MainFrame;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Use default look and feel
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
