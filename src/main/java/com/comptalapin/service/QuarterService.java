package com.comptalapin.service;

import com.comptalapin.model.*;
import com.comptalapin.persistence.OperationDao;
import com.comptalapin.persistence.QuarterDao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class QuarterService {
    private final QuarterDao quarterDao;
    private final OperationDao operationDao;

    public QuarterService() {
        this.quarterDao = new QuarterDao();
        this.operationDao = new OperationDao();
    }

    public List<Quarter> getAllQuarters() throws SQLException {
        return quarterDao.findAll();
    }

    public Quarter createQuarter(int year, int number) throws SQLException {
        Quarter quarter = new Quarter(year, number);
        return quarterDao.save(quarter);
    }

    public Quarter closeQuarter(Quarter quarter) throws SQLException {
        quarter.setStatus(QuarterStatus.CLOSED);
        return quarterDao.save(quarter);
    }

    public Quarter reopenQuarter(Quarter quarter) throws SQLException {
        quarter.setStatus(QuarterStatus.OPEN);
        return quarterDao.save(quarter);
    }

    public Operation addOperation(Operation operation) throws SQLException {
        return operationDao.save(operation);
    }

    public void deleteOperation(Long id) throws SQLException {
        operationDao.delete(id);
    }

    public List<Operation> getOperationsForQuarter(Quarter quarter) throws SQLException {
        return operationDao.findByQuarter(quarter);
    }

    public BigDecimal getTotalExpenses(Quarter quarter) throws SQLException {
        return getOperationsForQuarter(quarter).stream()
                .filter(op -> op.getType() == OperationType.EXPENSE)
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalIncome(Quarter quarter) throws SQLException {
        return getOperationsForQuarter(quarter).stream()
                .filter(op -> op.getType() == OperationType.INCOME)
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Determine the current quarter based on today's date, or return the latest open one.
     */
    public Quarter getCurrentQuarter() throws SQLException {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentNumber = (today.getMonthValue() - 1) / 3 + 1;

        List<Quarter> all = getAllQuarters();
        return all.stream()
                .filter(q -> q.getYear() == currentYear && q.getNumber() == currentNumber)
                .findFirst()
                .orElse(null);
    }
}
