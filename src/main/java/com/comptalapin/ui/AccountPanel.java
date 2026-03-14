package com.comptalapin.ui;

import com.comptalapin.model.Account;
import com.comptalapin.model.Quarter;
import com.comptalapin.service.AccountService;
import com.comptalapin.service.QuarterService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class AccountPanel extends JPanel {
    private final AccountService accountService;
    private final QuarterService quarterService;

    private DefaultTableModel accountTableModel;
    private JTable accountTable;
    private JTextArea forecastArea;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;

    public AccountPanel(AccountService accountService, QuarterService quarterService) {
        this.accountService = accountService;
        this.quarterService = quarterService;
        initComponents();
        refreshAccounts();
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Split pane: left = accounts, right = forecast
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);

        // Left: account table
        JPanel leftPanel = new JPanel(new BorderLayout(4, 4));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Comptes"));

        String[] cols = {"Compte", "Solde initial", "Solde courant"};
        accountTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        accountTable = new JTable(accountTableModel);
        accountTable.setFillsViewportHeight(true);
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftPanel.add(new JScrollPane(accountTable), BorderLayout.CENTER);

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("+ Ajouter");
        editButton = new JButton("Modifier");
        deleteButton = new JButton("Supprimer");
        refreshButton = new JButton("↻ Actualiser");
        addButton.addActionListener(e -> addAccount());
        editButton.addActionListener(e -> editAccount());
        deleteButton.addActionListener(e -> deleteAccount());
        refreshButton.addActionListener(e -> refreshAccounts());
        leftButtons.add(addButton);
        leftButtons.add(editButton);
        leftButtons.add(deleteButton);
        leftButtons.add(refreshButton);
        leftPanel.add(leftButtons, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);

        // Right: forecast panel
        JPanel rightPanel = new JPanel(new BorderLayout(4, 4));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Prévision de solde (trimestre courant)"));
        forecastArea = new JTextArea();
        forecastArea.setEditable(false);
        forecastArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        rightPanel.add(new JScrollPane(forecastArea), BorderLayout.CENTER);

        JButton forecastButton = new JButton("Calculer les prévisions");
        forecastButton.addActionListener(e -> computeForecast());
        rightPanel.add(forecastButton, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private void refreshAccounts() {
        accountTableModel.setRowCount(0);
        try {
            List<Account> accounts = accountService.getAllAccounts();
            List<Quarter> allQuarters = quarterService.getAllQuarters();
            for (Account account : accounts) {
                BigDecimal current = accountService.computeCurrentBalance(account, allQuarters);
                accountTableModel.addRow(new Object[]{
                        account.getName(),
                        FormatUtils.formatAmount(account.getInitialBalance()),
                        FormatUtils.formatAmount(current)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addAccount() {
        AccountDialog dialog = new AccountDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        Account result = dialog.getResult();
        if (result != null) {
            try {
                accountService.createAccount(result.getName(), result.getInitialBalance());
                refreshAccounts();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editAccount() {
        int row = accountTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un compte.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            List<Account> accounts = accountService.getAllAccounts();
            Account account = accounts.get(row);
            AccountDialog dialog = new AccountDialog((Frame) SwingUtilities.getWindowAncestor(this), account);
            dialog.setVisible(true);
            Account result = dialog.getResult();
            if (result != null) {
                accountService.updateAccount(result);
                refreshAccounts();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAccount() {
        int row = accountTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un compte.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer ce compte ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            List<Account> accounts = accountService.getAllAccounts();
            Account account = accounts.get(row);
            accountService.deleteAccount(account.getId());
            refreshAccounts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur (vérifiez qu'il n'y a pas d'opérations liées) : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void computeForecast() {
        forecastArea.setText("");
        try {
            Quarter currentQuarter = quarterService.getCurrentQuarter();
            if (currentQuarter == null) {
                forecastArea.setText("Aucun trimestre courant trouvé.\nCréez le trimestre courant dans l'onglet 'Trimestres'.");
                return;
            }
            List<Account> accounts = accountService.getAllAccounts();
            List<Quarter> allQuarters = quarterService.getAllQuarters();

            StringBuilder sb = new StringBuilder();
            sb.append("Prévisions pour ").append(currentQuarter.getLabel()).append("\n");
            sb.append("=".repeat(50)).append("\n\n");

            int[] months = currentQuarter.getMonths();

            // Header
            sb.append(String.format("%-20s", "Compte"));
            for (int m : months) {
                String monthName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.FRENCH);
                sb.append(String.format("%-16s", monthName));
            }
            sb.append("\n");
            sb.append("-".repeat(20 + 16 * months.length)).append("\n");

            for (Account account : accounts) {
                sb.append(String.format("%-20s", account.getName()));
                for (int m : months) {
                    BigDecimal forecast = accountService.forecastBalanceAtEndOfMonth(
                            account, allQuarters, currentQuarter, m);
                    sb.append(String.format("%-16s", String.format("%,.2f€", forecast)));
                }
                sb.append("\n");
            }

            forecastArea.setText(sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
