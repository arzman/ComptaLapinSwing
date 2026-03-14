package com.comptalapin.ui;

import com.comptalapin.model.Account;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class AccountDialog extends JDialog {
    private Account result;
    private JTextField nameField;
    private JTextField balanceField;

    public AccountDialog(Frame parent, Account existing) {
        super(parent, existing == null ? "Nouveau compte" : "Modifier le compte", true);
        initComponents(existing);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents(Account existing) {
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Nom :"), gbc);
        nameField = new JTextField(existing != null ? existing.getName() : "", 20);
        gbc.gridx = 1; form.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Solde initial (€) :"), gbc);
        balanceField = new JTextField(existing != null ? existing.getInitialBalance().toPlainString() : "0.00", 12);
        gbc.gridx = 1; form.add(balanceField, gbc);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Annuler");
        ok.addActionListener(e -> onOk(existing));
        cancel.addActionListener(e -> dispose());
        buttons.add(ok);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);
    }

    private void onOk(Account existing) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom du compte est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            BigDecimal balance = new BigDecimal(balanceField.getText().trim().replace(',', '.'));
            if (existing != null) {
                existing.setName(name);
                existing.setInitialBalance(balance);
                result = existing;
            } else {
                result = new Account(name, balance);
            }
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Solde invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Account getResult() {
        return result;
    }
}
