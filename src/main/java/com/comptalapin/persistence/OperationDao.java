package com.comptalapin.persistence;

import com.comptalapin.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OperationDao {
    private final Connection connection;
    private final AccountDao accountDao;
    private final QuarterDao quarterDao;

    public OperationDao() {
        this.connection = DatabaseManager.getInstance().getConnection();
        this.accountDao = new AccountDao();
        this.quarterDao = new QuarterDao();
    }

    public Operation save(Operation operation) throws SQLException {
        if (operation.getId() == null) {
            String sql = "INSERT INTO OPERATION (OP_DATE, DESCRIPTION, AMOUNT, TYPE, ACCOUNT_ID, TARGET_ACCOUNT_ID, QUARTER_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, Date.valueOf(operation.getDate()));
                ps.setString(2, operation.getDescription());
                ps.setBigDecimal(3, operation.getAmount());
                ps.setString(4, operation.getType().name());
                ps.setLong(5, operation.getAccount().getId());
                if (operation.getTargetAccount() != null) {
                    ps.setLong(6, operation.getTargetAccount().getId());
                } else {
                    ps.setNull(6, Types.BIGINT);
                }
                ps.setLong(7, operation.getQuarter().getId());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        operation.setId(rs.getLong(1));
                    }
                }
                connection.commit();
            }
        } else {
            String sql = "UPDATE OPERATION SET OP_DATE=?, DESCRIPTION=?, AMOUNT=?, TYPE=?, ACCOUNT_ID=?, TARGET_ACCOUNT_ID=?, QUARTER_ID=? WHERE ID=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(operation.getDate()));
                ps.setString(2, operation.getDescription());
                ps.setBigDecimal(3, operation.getAmount());
                ps.setString(4, operation.getType().name());
                ps.setLong(5, operation.getAccount().getId());
                if (operation.getTargetAccount() != null) {
                    ps.setLong(6, operation.getTargetAccount().getId());
                } else {
                    ps.setNull(6, Types.BIGINT);
                }
                ps.setLong(7, operation.getQuarter().getId());
                ps.setLong(8, operation.getId());
                ps.executeUpdate();
                connection.commit();
            }
        }
        return operation;
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM OPERATION WHERE ID=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            connection.commit();
        }
    }

    public List<Operation> findByQuarter(Quarter quarter) throws SQLException {
        List<Operation> list = new ArrayList<>();
        String sql = "SELECT * FROM OPERATION WHERE QUARTER_ID=? ORDER BY OP_DATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, quarter.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, quarter));
                }
            }
        }
        return list;
    }

    public List<Operation> findByAccountAndQuarter(Account account, Quarter quarter) throws SQLException {
        List<Operation> list = new ArrayList<>();
        String sql = "SELECT * FROM OPERATION WHERE (ACCOUNT_ID=? OR TARGET_ACCOUNT_ID=?) AND QUARTER_ID=? ORDER BY OP_DATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, account.getId());
            ps.setLong(2, account.getId());
            ps.setLong(3, quarter.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, quarter));
                }
            }
        }
        return list;
    }

    private Operation mapRow(ResultSet rs, Quarter quarter) throws SQLException {
        Operation op = new Operation();
        op.setId(rs.getLong("ID"));
        op.setDate(rs.getDate("OP_DATE").toLocalDate());
        op.setDescription(rs.getString("DESCRIPTION"));
        op.setAmount(rs.getBigDecimal("AMOUNT"));
        op.setType(OperationType.valueOf(rs.getString("TYPE")));
        op.setQuarter(quarter);

        long accountId = rs.getLong("ACCOUNT_ID");
        accountDao.findById(accountId).ifPresent(op::setAccount);

        long targetId = rs.getLong("TARGET_ACCOUNT_ID");
        if (!rs.wasNull()) {
            accountDao.findById(targetId).ifPresent(op::setTargetAccount);
        }
        return op;
    }
}
