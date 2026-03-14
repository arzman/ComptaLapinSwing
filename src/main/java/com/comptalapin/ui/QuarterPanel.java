package com.comptalapin.ui;

import com.comptalapin.model.*;
import com.comptalapin.service.AccountService;
import com.comptalapin.service.QuarterService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class QuarterPanel extends JPanel {
    private final QuarterService quarterService;
    private final AccountService accountService;

    private JComboBox<Quarter> quarterCombo;
    private JLabel totalExpensesLabel;
    private JLabel totalIncomeLabel;
    private JLabel balanceLabel;
    private OperationTableModel tableModel;
    private JTable operationTable;
    private JButton addButton;
    private JButton deleteButton;
    private JButton closeButton;
    private JButton newQuarterButton;

    public QuarterPanel(QuarterService quarterService, AccountService accountService) {
        this.quarterService = quarterService;
        this.accountService = accountService;
        initComponents();
        loadQuarters();
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel: quarter selection + actions
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topPanel.add(new JLabel("Trimestre :"));
        quarterCombo = new JComboBox<>();
        quarterCombo.setPreferredSize(new Dimension(180, 26));
        quarterCombo.addActionListener(e -> refreshOperations());
        topPanel.add(quarterCombo);

        newQuarterButton = new JButton("Nouveau trimestre");
        newQuarterButton.addActionListener(e -> createNewQuarter());
        topPanel.add(newQuarterButton);

        closeButton = new JButton("Clôturer / Rouvrir");
        closeButton.addActionListener(e -> toggleQuarterStatus());
        topPanel.add(closeButton);

        add(topPanel, BorderLayout.NORTH);

        // Center: operations table
        tableModel = new OperationTableModel();
        operationTable = new JTable(tableModel);
        operationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        operationTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(operationTable);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel: summary + operation actions
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));

        // Summary
        JPanel summaryPanel = new JPanel(new GridLayout(3, 2, 4, 4));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Synthèse du trimestre"));
        summaryPanel.add(new JLabel("Total dépenses :"));
        totalExpensesLabel = new JLabel("0,00 €");
        totalExpensesLabel.setForeground(Color.RED);
        summaryPanel.add(totalExpensesLabel);
        summaryPanel.add(new JLabel("Total recettes :"));
        totalIncomeLabel = new JLabel("0,00 €");
        totalIncomeLabel.setForeground(new Color(0, 128, 0));
        summaryPanel.add(totalIncomeLabel);
        summaryPanel.add(new JLabel("Solde net :"));
        balanceLabel = new JLabel("0,00 €");
        summaryPanel.add(balanceLabel);
        bottomPanel.add(summaryPanel, BorderLayout.WEST);

        // Action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("+ Ajouter opération");
        addButton.addActionListener(e -> addOperation());
        deleteButton = new JButton("Supprimer");
        deleteButton.addActionListener(e -> deleteOperation());
        actionsPanel.add(addButton);
        actionsPanel.add(deleteButton);
        bottomPanel.add(actionsPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadQuarters() {
        try {
            quarterCombo.removeAllItems();
            List<Quarter> quarters = quarterService.getAllQuarters();
            for (Quarter q : quarters) {
                quarterCombo.addItem(q);
            }
            refreshOperations();
        } catch (SQLException e) {
            showError("Erreur lors du chargement des trimestres : " + e.getMessage());
        }
    }

    private void refreshOperations() {
        Quarter selected = (Quarter) quarterCombo.getSelectedItem();
        if (selected == null) {
            tableModel.setOperations(null);
            updateSummary(null);
            return;
        }
        try {
            List<Operation> operations = quarterService.getOperationsForQuarter(selected);
            tableModel.setOperations(operations);
            updateSummary(selected);
            boolean isOpen = selected.getStatus() == QuarterStatus.OPEN;
            addButton.setEnabled(isOpen);
        } catch (SQLException e) {
            showError("Erreur lors du chargement des opérations : " + e.getMessage());
        }
    }

    private void updateSummary(Quarter quarter) {
        if (quarter == null) {
            totalExpensesLabel.setText("0,00 €");
            totalIncomeLabel.setText("0,00 €");
            balanceLabel.setText("0,00 €");
            return;
        }
        try {
            BigDecimal expenses = quarterService.getTotalExpenses(quarter);
            BigDecimal income = quarterService.getTotalIncome(quarter);
            BigDecimal balance = income.subtract(expenses);
            totalExpensesLabel.setText(FormatUtils.formatAmount(expenses));
            totalIncomeLabel.setText(FormatUtils.formatAmount(income));
            balanceLabel.setText(FormatUtils.formatAmount(balance));
            balanceLabel.setForeground(balance.compareTo(BigDecimal.ZERO) >= 0 ? new Color(0, 128, 0) : Color.RED);
        } catch (SQLException e) {
            showError("Erreur lors du calcul de la synthèse : " + e.getMessage());
        }
    }

    private void createNewQuarter() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int number = (today.getMonthValue() - 1) / 3 + 1;

        String input = JOptionPane.showInputDialog(this,
                "Année et numéro de trimestre (ex: 2024-1) :",
                year + "-" + number);
        if (input == null || input.trim().isEmpty()) return;

        try {
            String[] parts = input.trim().split("-");
            int y = Integer.parseInt(parts[0].trim());
            int n = Integer.parseInt(parts[1].trim());
            if (n < 1 || n > 4) {
                JOptionPane.showMessageDialog(this, "Le numéro de trimestre doit être entre 1 et 4.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            quarterService.createQuarter(y, n);
            loadQuarters();
        } catch (Exception e) {
            showError("Format invalide. Utilisez AAAA-N (ex: 2024-1)");
        }
    }

    private void toggleQuarterStatus() {
        Quarter selected = (Quarter) quarterCombo.getSelectedItem();
        if (selected == null) return;
        try {
            if (selected.getStatus() == QuarterStatus.OPEN) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Clôturer le trimestre " + selected.getLabel() + " ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    quarterService.closeQuarter(selected);
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Rouvrir le trimestre " + selected.getLabel() + " ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    quarterService.reopenQuarter(selected);
                }
            }
            loadQuarters();
        } catch (SQLException e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    private void addOperation() {
        Quarter selected = (Quarter) quarterCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un trimestre.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (selected.getStatus() != QuarterStatus.OPEN) {
            JOptionPane.showMessageDialog(this, "Ce trimestre est clôturé. Rouvrez-le pour ajouter des opérations.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            List<Account> accounts = accountService.getAllAccounts();
            if (accounts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun compte disponible. Créez d'abord un compte dans l'onglet 'Comptes'.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            OperationDialog dialog = new OperationDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    selected, accounts, quarterService);
            dialog.setVisible(true);
            Operation op = dialog.getResult();
            if (op != null) {
                quarterService.addOperation(op);
                refreshOperations();
            }
        } catch (SQLException e) {
            showError("Erreur lors de l'ajout de l'opération : " + e.getMessage());
        }
    }

    private void deleteOperation() {
        int row = operationTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une opération.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Quarter selected = (Quarter) quarterCombo.getSelectedItem();
        if (selected != null && selected.getStatus() != QuarterStatus.OPEN) {
            JOptionPane.showMessageDialog(this, "Ce trimestre est clôturé.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Operation op = tableModel.getOperationAt(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer cette opération ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                quarterService.deleteOperation(op.getId());
                refreshOperations();
            } catch (SQLException e) {
                showError("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
