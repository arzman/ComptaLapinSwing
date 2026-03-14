package com.comptalapin.service;

import com.comptalapin.model.*;
import com.comptalapin.persistence.DatabaseManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.*;

public class QuarterServiceTest {
    private QuarterService quarterService;
    private AccountService accountService;

    @Before
    public void setUp() throws Exception {
        System.setProperty("comptalapin.db.url", "jdbc:hsqldb:mem:testdb_" + System.nanoTime());
        DatabaseManager.resetInstance();
        quarterService = new QuarterService();
        accountService = new AccountService();
    }

    @After
    public void tearDown() throws Exception {
        DatabaseManager.resetInstance();
    }

    @Test
    public void testCreateQuarter() throws Exception {
        Quarter q = quarterService.createQuarter(2024, 1);
        assertNotNull(q.getId());
        assertEquals(2024, q.getYear());
        assertEquals(1, q.getNumber());
        assertEquals(QuarterStatus.OPEN, q.getStatus());
    }

    @Test
    public void testCloseAndReopenQuarter() throws Exception {
        Quarter q = quarterService.createQuarter(2024, 2);
        quarterService.closeQuarter(q);
        assertEquals(QuarterStatus.CLOSED, q.getStatus());

        quarterService.reopenQuarter(q);
        assertEquals(QuarterStatus.OPEN, q.getStatus());
    }

    @Test
    public void testAddOperationAndSummary() throws Exception {
        Quarter q = quarterService.createQuarter(2024, 3);
        Account account = accountService.createAccount("Compte Test", new BigDecimal("1000.00"));

        Operation income = new Operation();
        income.setDate(LocalDate.of(2024, 7, 15));
        income.setDescription("Salaire");
        income.setAmount(new BigDecimal("2000.00"));
        income.setType(OperationType.INCOME);
        income.setAccount(account);
        income.setQuarter(q);
        quarterService.addOperation(income);

        Operation expense = new Operation();
        expense.setDate(LocalDate.of(2024, 8, 5));
        expense.setDescription("Loyer");
        expense.setAmount(new BigDecimal("800.00"));
        expense.setType(OperationType.EXPENSE);
        expense.setAccount(account);
        expense.setQuarter(q);
        quarterService.addOperation(expense);

        BigDecimal totalIncome = quarterService.getTotalIncome(q);
        BigDecimal totalExpenses = quarterService.getTotalExpenses(q);

        assertEquals(new BigDecimal("2000.00"), totalIncome);
        assertEquals(new BigDecimal("800.00"), totalExpenses);
    }

    @Test
    public void testAccountBalance() throws Exception {
        Account account = accountService.createAccount("Compte Courant", new BigDecimal("500.00"));
        Quarter q = quarterService.createQuarter(2024, 4);

        Operation op = new Operation();
        op.setDate(LocalDate.of(2024, 10, 1));
        op.setDescription("Test");
        op.setAmount(new BigDecimal("300.00"));
        op.setType(OperationType.INCOME);
        op.setAccount(account);
        op.setQuarter(q);
        quarterService.addOperation(op);

        java.util.List<Quarter> allQuarters = quarterService.getAllQuarters();
        BigDecimal balance = accountService.computeCurrentBalance(account, allQuarters);
        assertEquals(new BigDecimal("800.00"), balance);
    }
}
