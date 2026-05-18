package com.quanlydatvemaybay.dao;

import com.quanlydatvemaybay.config.DatabaseConfig;
import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.enums.FlightStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FlightDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    private Flight mapRow(ResultSet rs) throws SQLException {
        Flight f = new Flight();
        f.setId(rs.getLong("ID"));
        f.setFlightCode(rs.getString("FLIGHT_CODE"));
        f.setAirline(rs.getString("AIRLINE"));
        f.setDepartureAirport(rs.getString("DEPARTURE_AIRPORT"));
        f.setArrivalAirport(rs.getString("ARRIVAL_AIRPORT"));
        Timestamp depTs = rs.getTimestamp("DEPARTURE_TIME");
        if (depTs != null) f.setDepartureTime(depTs.toLocalDateTime());
        Timestamp arrTs = rs.getTimestamp("ARRIVAL_TIME");
        if (arrTs != null) f.setArrivalTime(arrTs.toLocalDateTime());
        f.setTotalSeats(rs.getInt("TOTAL_SEATS"));
        f.setAvailableSeats(rs.getInt("AVAILABLE_SEATS"));
        f.setPrice(rs.getBigDecimal("PRICE"));
        String status = rs.getString("STATUS");
        if (status != null) f.setStatus(FlightStatus.valueOf(status));
        Timestamp createdTs = rs.getTimestamp("CREATED_DATE");
        if (createdTs != null) f.setCreatedDate(createdTs.toLocalDateTime());
        Timestamp updatedTs = rs.getTimestamp("UPDATED_DATE");
        if (updatedTs != null) f.setUpdatedDate(updatedTs.toLocalDateTime());
        return f;
    }

    public List<Flight> findAll() throws SQLException {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT * FROM FLIGHT ORDER BY ID DESC";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Flight> search(String departure, String arrival, String airline) throws SQLException {
        List<Flight> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM FLIGHT WHERE 1=1");
        if (departure != null && !departure.isEmpty())
            sql.append(" AND UPPER(DEPARTURE_AIRPORT) LIKE UPPER(?)");
        if (arrival != null && !arrival.isEmpty())
            sql.append(" AND UPPER(ARRIVAL_AIRPORT) LIKE UPPER(?)");
        if (airline != null && !airline.isEmpty())
            sql.append(" AND UPPER(AIRLINE) LIKE UPPER(?)");
        sql.append(" ORDER BY ID DESC");

        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            if (departure != null && !departure.isEmpty()) ps.setString(idx++, "%" + departure + "%");
            if (arrival != null && !arrival.isEmpty()) ps.setString(idx++, "%" + arrival + "%");
            if (airline != null && !airline.isEmpty()) ps.setString(idx++, "%" + airline + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Flight> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM FLIGHT WHERE ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Flight> findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM FLIGHT WHERE FLIGHT_CODE = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Flight save(Flight f) throws SQLException {
        String sql = "INSERT INTO FLIGHT (FLIGHT_CODE, AIRLINE, DEPARTURE_AIRPORT, ARRIVAL_AIRPORT, " +
                "DEPARTURE_TIME, ARRIVAL_TIME, TOTAL_SEATS, AVAILABLE_SEATS, PRICE, STATUS, CREATED_DATE) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, new String[]{"ID"})) {
            ps.setString(1, f.getFlightCode());
            ps.setString(2, f.getAirline());
            ps.setString(3, f.getDepartureAirport());
            ps.setString(4, f.getArrivalAirport());
            ps.setTimestamp(5, Timestamp.valueOf(f.getDepartureTime()));
            ps.setTimestamp(6, Timestamp.valueOf(f.getArrivalTime()));
            ps.setInt(7, f.getTotalSeats());
            // FIX: dùng availableSeats truyền vào thay vì luôn = totalSeats
            int avail = f.getAvailableSeats() > 0 ? f.getAvailableSeats() : f.getTotalSeats();
            if (avail > f.getTotalSeats()) avail = f.getTotalSeats();
            ps.setInt(8, avail);
            ps.setBigDecimal(9, f.getPrice());
            ps.setString(10, f.getStatus() != null ? f.getStatus().name() : FlightStatus.SCHEDULED.name());
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) f.setId(rs.getLong(1));
            }
        }
        return f;
    }

    public void update(Flight f) throws SQLException {
        String sql = "UPDATE FLIGHT SET FLIGHT_CODE=?, AIRLINE=?, DEPARTURE_AIRPORT=?, ARRIVAL_AIRPORT=?, " +
                "DEPARTURE_TIME=?, ARRIVAL_TIME=?, TOTAL_SEATS=?, AVAILABLE_SEATS=?, PRICE=?, STATUS=?, UPDATED_DATE=? " +
                "WHERE ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, f.getFlightCode());
            ps.setString(2, f.getAirline());
            ps.setString(3, f.getDepartureAirport());
            ps.setString(4, f.getArrivalAirport());
            ps.setTimestamp(5, Timestamp.valueOf(f.getDepartureTime()));
            ps.setTimestamp(6, Timestamp.valueOf(f.getArrivalTime()));
            ps.setInt(7, f.getTotalSeats());
            ps.setInt(8, f.getAvailableSeats());
            ps.setBigDecimal(9, f.getPrice());
            ps.setString(10, f.getStatus().name());
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(12, f.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Long id, FlightStatus status) throws SQLException {
        String sql = "UPDATE FLIGHT SET STATUS=?, UPDATED_DATE=? WHERE ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void updateAvailableSeats(Long id, int availableSeats) throws SQLException {
        String sql = "UPDATE FLIGHT SET AVAILABLE_SEATS=?, UPDATED_DATE=? WHERE ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, availableSeats);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM FLIGHT WHERE ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existsByCode(String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM FLIGHT WHERE FLIGHT_CODE=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean existsByCodeExcludeId(String code, Long id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM FLIGHT WHERE FLIGHT_CODE=? AND ID != ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // ======= TRANSACTION-AWARE OVERLOADS (cho BookingService) =======

    public Optional<Flight> findByIdForUpdate(Connection conn, Long id) throws SQLException {
        String sql = "SELECT * FROM FLIGHT WHERE ID = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public void updateAvailableSeats(Connection conn, Long id, int availableSeats) throws SQLException {
        String sql = "UPDATE FLIGHT SET AVAILABLE_SEATS=?, UPDATED_DATE=? WHERE ID=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, availableSeats);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    // ======= SMART SEARCH (cho tính năng tìm thông minh) =======

    /**
     * Tìm chuyến bay theo điểm đi/đến và khoảng ngày (departureDate ± dayRange).
     * Chỉ trả về các chuyến SCHEDULED và còn ghế trống. Sắp xếp theo
     * khoảng cách thời gian gần với mốc ngày yêu cầu.
     */
    public List<Flight> searchSmart(String departure, String arrival,
                                    LocalDateTime targetDate, int dayRangeBefore, int dayRangeAfter) throws SQLException {
        List<Flight> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM FLIGHT WHERE STATUS = 'SCHEDULED' AND AVAILABLE_SEATS > 0");
        if (departure != null && !departure.isEmpty())
            sql.append(" AND UPPER(DEPARTURE_AIRPORT) LIKE UPPER(?)");
        if (arrival != null && !arrival.isEmpty())
            sql.append(" AND UPPER(ARRIVAL_AIRPORT) LIKE UPPER(?)");
        if (targetDate != null) {
            sql.append(" AND DEPARTURE_TIME BETWEEN ? AND ?");
        }
        sql.append(" ORDER BY DEPARTURE_TIME ASC");

        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            if (departure != null && !departure.isEmpty())
                ps.setString(idx++, "%" + departure + "%");
            if (arrival != null && !arrival.isEmpty())
                ps.setString(idx++, "%" + arrival + "%");
            if (targetDate != null) {
                LocalDateTime from = targetDate.toLocalDate().atStartOfDay().minusDays(dayRangeBefore);
                LocalDateTime to = targetDate.toLocalDate().atTime(23, 59, 59).plusDays(dayRangeAfter);
                ps.setTimestamp(idx++, Timestamp.valueOf(from));
                ps.setTimestamp(idx++, Timestamp.valueOf(to));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public String generateNextCode(String airlinePrefix) throws SQLException {
        String sql = "SELECT FLIGHT_CODE FROM FLIGHT WHERE FLIGHT_CODE LIKE ?";
        int maxNum = 0;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, airlinePrefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString(1);
                    try {
                        int num = Integer.parseInt(code.substring(airlinePrefix.length()));
                        if (num > maxNum) maxNum = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return String.format("%s%03d", airlinePrefix, maxNum + 1);
    }
}
