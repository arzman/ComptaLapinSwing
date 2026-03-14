package com.comptalapin.persistence;

import com.comptalapin.model.Quarter;
import com.comptalapin.model.QuarterStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuarterDao {
    private final Connection connection;

    public QuarterDao() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public Quarter save(Quarter quarter) throws SQLException {
        if (quarter.getId() == null) {
            String sql = "INSERT INTO QUARTER (YEAR, NUMBER, STATUS) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, quarter.getYear());
                ps.setInt(2, quarter.getNumber());
                ps.setString(3, quarter.getStatus().name());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        quarter.setId(rs.getLong(1));
                    }
                }
                connection.commit();
            }
        } else {
            String sql = "UPDATE QUARTER SET YEAR=?, NUMBER=?, STATUS=? WHERE ID=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, quarter.getYear());
                ps.setInt(2, quarter.getNumber());
                ps.setString(3, quarter.getStatus().name());
                ps.setLong(4, quarter.getId());
                ps.executeUpdate();
                connection.commit();
            }
        }
        return quarter;
    }

    public Optional<Quarter> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM QUARTER WHERE ID=?";
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

    public List<Quarter> findAll() throws SQLException {
        List<Quarter> list = new ArrayList<>();
        String sql = "SELECT * FROM QUARTER ORDER BY YEAR DESC, NUMBER DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Quarter mapRow(ResultSet rs) throws SQLException {
        Quarter q = new Quarter();
        q.setId(rs.getLong("ID"));
        q.setYear(rs.getInt("YEAR"));
        q.setNumber(rs.getInt("NUMBER"));
        q.setStatus(QuarterStatus.valueOf(rs.getString("STATUS")));
        return q;
    }
}
