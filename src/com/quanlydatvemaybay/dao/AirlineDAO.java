package com.quanlydatvemaybay.dao;

import com.quanlydatvemaybay.config.DatabaseConfig;
import com.quanlydatvemaybay.entity.Airline;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AirlineDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    private Airline mapRow(ResultSet rs) throws SQLException {
        return new Airline(rs.getLong("ID"), rs.getString("CODE"), rs.getString("NAME"));
    }

    public List<Airline> findAll() throws SQLException {
        List<Airline> list = new ArrayList<>();
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM AIRLINE ORDER BY CODE")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Airline> findByCode(String code) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM AIRLINE WHERE CODE = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Airline save(Airline a) throws SQLException {
        String sql = "INSERT INTO AIRLINE (CODE, NAME) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, new String[]{"ID"})) {
            ps.setString(1, a.getCode().toUpperCase().trim());
            ps.setString(2, a.getName().trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) a.setId(rs.getLong(1));
            }
        }
        return a;
    }

    public void update(Airline a) throws SQLException {
        String sql = "UPDATE AIRLINE SET CODE = ?, NAME = ? WHERE ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getCode().toUpperCase().trim());
            ps.setString(2, a.getName().trim());
            ps.setLong(3, a.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM AIRLINE WHERE ID = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existsByCode(String code) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM AIRLINE WHERE UPPER(CODE) = UPPER(?)")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existsByCodeExcludeId(String code, Long id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM AIRLINE WHERE UPPER(CODE) = UPPER(?) AND ID != ?")) {
            ps.setString(1, code);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
