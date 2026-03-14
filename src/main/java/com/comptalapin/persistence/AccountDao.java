package com.comptalapin.persistence;

import com.comptalapin.model.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDao {
    private final Connection connection;

    public AccountDao() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public Account save(Account account) throws SQLException {
        if (account.getId() == null) {
            String sql = "INSERT INTO ACCOUNT (NAME, INITIAL_BALANCE) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, account.getName());
                ps.setBigDecimal(2, account.getInitialBalance());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        account.setId(rs.getLong(1));
                    }
                }
                connection.commit();
            }
        } else {
            String sql = "UPDATE ACCOUNT SET NAME=?, INITIAL_BALANCE=? WHERE ID=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, account.getName());
                ps.setBigDecimal(2, account.getInitialBalance());
                ps.setLong(3, account.getId());
                ps.executeUpdate();
                connection.commit();
            }
        }
        return account;
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM ACCOUNT WHERE ID=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            connection.commit();
        }
    }

    public Optional<Account> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM ACCOUNT WHERE ID=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Account> findAll() throws SQLException {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM ACCOUNT ORDER BY NAME";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setId(rs.getLong("ID"));
        a.setName(rs.getString("NAME"));
        a.setInitialBalance(rs.getBigDecimal("INITIAL_BALANCE"));
        return a;
    }
}
