package com.comptalapin.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Operation {
    private Long id;
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private OperationType type;
    private Account account;
    private Account targetAccount; // for TRANSFER only
    private Quarter quarter;

    public Operation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public OperationType getType() { return type; }
    public void setType(OperationType type) { this.type = type; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public Account getTargetAccount() { return targetAccount; }
    public void setTargetAccount(Account targetAccount) { this.targetAccount = targetAccount; }

    public Quarter getQuarter() { return quarter; }
    public void setQuarter(Quarter quarter) { this.quarter = quarter; }
}
