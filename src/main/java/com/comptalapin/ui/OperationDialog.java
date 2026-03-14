package com.comptalapin.ui;

import com.comptalapin.model.*;
import com.comptalapin.service.QuarterService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class OperationDialog extends JDialog {
    private final Quarter quarter;
    private final List<Account> accounts;
    private final QuarterService quarterService;
    private Operation result;

    private JTextField dateField;
    private JTextField descriptionField;
    private JComboBox<OperationType> typeCombo;
    private JComboBox<Account> accountCombo;
    private JComboBox<Account> targetAccountCombo;
    private JTextField amountField;
    private JLabel targetLabel;

    public OperationDialog(Frame parent, Quarter quarter, List<Account> accounts, QuarterService quarterService) {
        super(parent, "Nouvelle opération", true);
        this.quarter = quarter;
        this.accounts = accounts;
        this.quarterService = quarterService;
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Date
        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Date (YYYY-MM-DD) :"), gbc);
        dateField = new JTextField(LocalDate.now().toString(), 12);
        gbc.gridx = 1; form.add(dateField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Description :"), gbc);
        descriptionField = new JTextField(20);
        gbc.gridx = 1; form.add(descriptionField, gbc);

        // Type
        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Type :"), gbc);
        typeCombo = new JComboBox<>(OperationType.values());
        gbc.gridx = 1; form.add(typeCombo, gbc);

        // Account
        gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("Compte :"), gbc);
        accountCombo = new JComboBox<>(accounts.toArray(new Account[0]));
        gbc.gridx = 1; form.add(accountCombo, gbc);

        // Target account (for TRANSFER)
        targetLabel = new JLabel("Compte cible :");
        gbc.gridx = 0; gbc.gridy = 4; form.add(targetLabel, gbc);
        targetAccountCombo = new JComboBox<>(accounts.toArray(new Account[0]));
        gbc.gridx = 1; form.add(targetAccountCombo, gbc);

        // Amount
        gbc.gridx = 0; gbc.gridy = 5; form.add(new JLabel("Montant (€) :"), gbc);
        amountField = new JTextField("0.00", 10);
        gbc.gridx = 1; form.add(amountField, gbc);

        // Show/hide target account based on type
        typeCombo.addActionListener(e -> updateTargetVisibility());
        updateTargetVisibility();

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Annuler");
        ok.addActionListener(e -> onOk());
        cancel.addActionListener(e -> dispose());
        buttons.add(ok);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);
    }

    private void updateTargetVisibility() {
        boolean isTransfer = typeCombo.getSelectedItem() == OperationType.TRANSFER;
        targetLabel.setVisible(isTransfer);
        targetAccountCombo.setVisible(isTransfer);
    }

    private void onOk() {
        try {
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            BigDecimal amount = new BigDecimal(amountField.getText().trim().replace(',', '.'));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Le montant doit être positif.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Account account = (Account) accountCombo.getSelectedItem();
            if (account == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un compte.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            OperationType type = (OperationType) typeCombo.getSelectedItem();
            Account targetAccount = null;
            if (type == OperationType.TRANSFER) {
                targetAccount = (Account) targetAccountCombo.getSelectedItem();
                if (targetAccount == null || targetAccount.getId().equals(account.getId())) {
                    JOptionPane.showMessageDialog(this, "Veuillez sélectionner un compte cible différent.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            result = new Operation();
            result.setDate(date);
            result.setDescription(descriptionField.getText().trim());
            result.setAmount(amount);
            result.setType(type);
            result.setAccount(account);
            result.setTargetAccount(targetAccount);
            result.setQuarter(quarter);

            dispose();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format de date invalide. Utilisez YYYY-MM-DD.", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Montant invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Operation getResult() {
        return result;
    }
}
