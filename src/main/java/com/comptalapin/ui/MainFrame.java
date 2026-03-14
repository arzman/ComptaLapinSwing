package com.comptalapin.ui;

import com.comptalapin.persistence.DatabaseManager;
import com.comptalapin.service.AccountService;
import com.comptalapin.service.QuarterService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private final QuarterService quarterService;
    private final AccountService accountService;

    public MainFrame() {
        this.quarterService = new QuarterService();
        this.accountService = new AccountService();
        initComponents();
    }

    private void initComponents() {
        setTitle("ComptaLapin - Comptabilité Personnelle");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(900, 600));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseManager.getInstance().shutdown();
                System.exit(0);
            }
        });

        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Trimestres", new ImageIcon(), new QuarterPanel(quarterService, accountService), "Suivi trimestriel des opérations");
        tabbedPane.addTab("Comptes", new ImageIcon(), new AccountPanel(accountService, quarterService), "Suivi et prévisions de solde des comptes");

        add(tabbedPane);
        pack();
        setLocationRelativeTo(null);
    }
}
