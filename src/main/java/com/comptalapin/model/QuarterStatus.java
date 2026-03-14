package com.comptalapin.model;

public enum QuarterStatus {
    OPEN("Ouvert"),
    CLOSED("Clôturé");

    private final String label;

    QuarterStatus(String label) {
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
