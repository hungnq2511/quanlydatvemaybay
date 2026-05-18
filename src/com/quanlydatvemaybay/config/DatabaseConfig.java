package com.quanlydatvemaybay.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String URL = "jdbc:oracle:thin:@localhost:1521/ORCLPDB1";
    private static final String USERNAME = "quanLyDatVeMayBay";
    private static final String PASSWORD = "123456";

    private static DatabaseConfig instance;
    private Connection sharedConnection;

    private DatabaseConfig() {}

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * Connection chia sẻ – dùng cho các thao tác đọc đơn giản (DAO hiện tại).
     * KHÔNG dùng cho transaction nhiều bước có race condition.
     */
    public synchronized Connection getConnection() throws SQLException {
        if (sharedConnection == null || sharedConnection.isClosed()) {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                sharedConnection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Oracle JDBC Driver not found: " + e.getMessage());
            }
        }
        return sharedConnection;
    }

    /**
     * Tạo Connection mới độc lập – BẮT BUỘC dùng cho các thao tác có transaction
     * (đặt vé, hủy vé) để có thể setAutoCommit(false) + SELECT FOR UPDATE.
     * Caller phải tự đóng connection (try-with-resources).
     */
    public Connection newConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC Driver not found: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (sharedConnection != null && !sharedConnection.isClosed()) {
                sharedConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
