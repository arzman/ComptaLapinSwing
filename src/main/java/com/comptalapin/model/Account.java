package com.comptalapin.model;

import java.math.BigDecimal;

public class Account {
    private Long id;
    private String name;
    private BigDecimal initialBalance;

    public Account() {
        this.initialBalance = BigDecimal.ZERO;
    }

    public Account(String name, BigDecimal initialBalance) {
        this.name = name;
        this.initialBalance = initialBalance;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    @Override
    public String toString() {
        return name;
    }
}
