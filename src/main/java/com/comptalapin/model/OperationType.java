package com.comptalapin.model;

public enum OperationType {
    EXPENSE("Dépense"),
    INCOME("Recette"),
    TRANSFER("Transfert");

    private final String label;

    OperationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
