package com.comptalapin.service;

import com.comptalapin.model.*;
import com.comptalapin.persistence.AccountDao;
import com.comptalapin.persistence.OperationDao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AccountService {
    private final AccountDao accountDao;
    private final OperationDao operationDao;

    public AccountService() {
        this.accountDao = new AccountDao();
        this.operationDao = new OperationDao();
    }

    public List<Account> getAllAccounts() throws SQLException {
        return accountDao.findAll();
    }

    public Account createAccount(String name, BigDecimal initialBalance) throws SQLException {
        Account account = new Account(name, initialBalance);
        return accountDao.save(account);
    }

    public Account updateAccount(Account account) throws SQLException {
        return accountDao.save(account);
    }

    public void deleteAccount(Long id) throws SQLException {
        accountDao.delete(id);
    }

    /**
     * Compute the current balance for an account based on all operations up to today.
     */
    public BigDecimal computeCurrentBalance(Account account, List<Quarter> allQuarters) throws SQLException {
        BigDecimal balance = account.getInitialBalance();
        for (Quarter quarter : allQuarters) {
            List<Operation> ops = operationDao.findByAccountAndQuarter(account, quarter);
            balance = applyOperations(account, balance, ops, null);
        }
        return balance;
    }

    /**
     * Forecast balance at end of a given month (in the current quarter).
     */
    public BigDecimal forecastBalanceAtEndOfMonth(Account account, List<Quarter> allQuarters,
                                                    Quarter currentQuarter, int targetMonth) throws SQLException {
        BigDecimal balance = account.getInitialBalance();
        // Apply all past quarters fully (quarters with earlier year/number than currentQuarter)
        for (Quarter quarter : allQuarters) {
            boolean isBefore = quarter.getYear() < currentQuarter.getYear() ||
                    (quarter.getYear() == currentQuarter.getYear() && quarter.getNumber() < currentQuarter.getNumber());
            if (!isBefore) continue;
            List<Operation> ops = operationDao.findByAccountAndQuarter(account, quarter);
            balance = applyOperations(account, balance, ops, null);
        }
        // Apply operations up to end of targetMonth in current quarter
        LocalDate firstOfMonth = LocalDate.of(currentQuarter.getYear(), targetMonth, 1);
        LocalDate cutoff = firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth());
        List<Operation> currentOps = operationDao.findByAccountAndQuarter(account, currentQuarter);
        balance = applyOperations(account, balance, currentOps, cutoff);
        return balance;
    }

    private BigDecimal applyOperations(Account account, BigDecimal balance,
                                        List<Operation> ops, LocalDate cutoffDate) {
        for (Operation op : ops) {
            if (cutoffDate != null && op.getDate().isAfter(cutoffDate)) continue;
            switch (op.getType()) {
                case INCOME:
                    if (account.getId().equals(op.getAccount().getId())) {
                        balance = balance.add(op.getAmount());
                    }
                    break;
                case EXPENSE:
                    if (account.getId().equals(op.getAccount().getId())) {
                        balance = balance.subtract(op.getAmount());
                    }
                    break;
                case TRANSFER:
                    if (account.getId().equals(op.getAccount().getId())) {
                        balance = balance.subtract(op.getAmount());
                    } else if (op.getTargetAccount() != null && account.getId().equals(op.getTargetAccount().getId())) {
                        balance = balance.add(op.getAmount());
                    }
                    break;
            }
        }
        return balance;
    }
}
