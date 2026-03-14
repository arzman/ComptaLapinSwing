package com.comptalapin.ui;

import com.comptalapin.model.Operation;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class OperationTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Date", "Description", "Type", "Compte", "Compte cible", "Montant"};
    private List<Operation> operations = new ArrayList<>();

    public void setOperations(List<Operation> operations) {
        this.operations = operations != null ? operations : new ArrayList<>();
        fireTableDataChanged();
    }

    public Operation getOperationAt(int row) {
        return operations.get(row);
    }

    @Override
    public int getRowCount() {
        return operations.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Operation op = operations.get(rowIndex);
        switch (columnIndex) {
            case 0: return op.getDate().toString();
            case 1: return op.getDescription() != null ? op.getDescription() : "";
            case 2: return op.getType().getLabel();
            case 3: return op.getAccount() != null ? op.getAccount().getName() : "";
            case 4: return op.getTargetAccount() != null ? op.getTargetAccount().getName() : "";
            case 5: return op.getAmount().toPlainString() + " €";
            default: return "";
        }
    }
}
