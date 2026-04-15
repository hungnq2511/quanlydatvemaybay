package com.quanlydatvemaybay.dao;

import com.quanlydatvemaybay.config.DatabaseConfig;
import com.quanlydatvemaybay.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("ID"));
        u.setUserName(rs.getString("USER_NAME"));
        u.setPassword(rs.getString("PASSWORD"));
        u.setFullName(rs.getString("FULL_NAME"));
        u.setEmail(rs.getString("EMAIL"));
        u.setSdt(rs.getString("SDT"));
        int statusVal = rs.getInt("STATUS");
        u.setStatus(statusVal == 1);
        return u;
    }

    public Optional<User> findByUserName(String userName) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE USER_NAME=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = mapRow(rs);
                    u.setRole(findRoleByUserId(u.getId()));
                    return Optional.of(u);
                }
            }
        }
        return Optional.empty();
    }

    public String findRoleByUserId(Long userId) throws SQLException {
        String sql = "SELECT ROLE_ID FROM USER_ROLE WHERE USER_ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("ROLE_ID");
            }
        }
        return null;
    }

    public void saveUserRole(Long userId, String roleId) throws SQLException {
        String sql = "INSERT INTO USER_ROLE (USER_ID, ROLE_ID, CREATED_DATE) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, roleId);
            ps.setTimestamp(3, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM USERS ORDER BY ID DESC";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public User save(User u) throws SQLException {
        String sql = "INSERT INTO USERS (USER_NAME, PASSWORD, FULL_NAME, EMAIL, SDT, STATUS, CREATED_DATE) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, new String[]{"ID"})) {
            ps.setString(1, u.getUserName());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getSdt());
            ps.setInt(6, Boolean.TRUE.equals(u.getStatus()) ? 1 : 0);
            ps.setTimestamp(7, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getLong(1));
            }
        }
        return u;
    }

    public boolean existsByUserName(String userName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM USERS WHERE USER_NAME=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}
