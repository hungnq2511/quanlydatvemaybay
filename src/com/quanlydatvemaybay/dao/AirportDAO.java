package com.quanlydatvemaybay.dao;

import com.quanlydatvemaybay.config.DatabaseConfig;
import com.quanlydatvemaybay.entity.Airport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AirportDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    private Airport mapRow(ResultSet rs) throws SQLException {
        return new Airport(rs.getLong("ID"), rs.getString("CODE"),
                rs.getString("NAME"), rs.getString("CITY"));
    }

    public List<Airport> findAll() throws SQLException {
        List<Airport> list = new ArrayList<>();
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM AIRPORT ORDER BY CODE")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Airport> findByCode(String code) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM AIRPORT WHERE CODE = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Airport save(Airport a) throws SQLException {
        String sql = "INSERT INTO AIRPORT (CODE, NAME, CITY) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, new String[]{"ID"})) {
            ps.setString(1, a.getCode().toUpperCase().trim());
            ps.setString(2, a.getName().trim());
            ps.setString(3, a.getCity() != null ? a.getCity().trim() : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) a.setId(rs.getLong(1));
            }
        }
        return a;
    }

    public void update(Airport a) throws SQLException {
        String sql = "UPDATE AIRPORT SET CODE = ?, NAME = ?, CITY = ? WHERE ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getCode().toUpperCase().trim());
            ps.setString(2, a.getName().trim());
            ps.setString(3, a.getCity() != null ? a.getCity().trim() : null);
            ps.setLong(4, a.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM AIRPORT WHERE ID = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existsByCode(String code) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM AIRPORT WHERE UPPER(CODE) = UPPER(?)")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existsByCodeExcludeId(String code, Long id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM AIRPORT WHERE UPPER(CODE) = UPPER(?) AND ID != ?")) {
            ps.setString(1, code);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
