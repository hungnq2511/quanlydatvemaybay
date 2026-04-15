package com.quanlydatvemaybay.dao;

import com.quanlydatvemaybay.config.DatabaseConfig;
import com.quanlydatvemaybay.entity.Booking;
import com.quanlydatvemaybay.enums.BookingStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getLong("ID"));
        b.setBookingCode(rs.getString("BOOKING_CODE"));
        b.setTicketId(rs.getLong("TICKET_ID"));
        b.setSeatNumber(rs.getString("SEAT_NUMBER"));
        b.setFlightId(rs.getLong("FLIGHT_ID"));
        b.setFlightCode(rs.getString("FLIGHT_CODE"));
        b.setDepartureAirport(rs.getString("DEPARTURE_AIRPORT"));
        b.setArrivalAirport(rs.getString("ARRIVAL_AIRPORT"));
        Timestamp depTs = rs.getTimestamp("DEPARTURE_TIME");
        if (depTs != null) b.setDepartureTime(depTs.toLocalDateTime());
        b.setPassengerName(rs.getString("PASSENGER_NAME"));
        b.setPassengerEmail(rs.getString("PASSENGER_EMAIL"));
        b.setPassengerPhone(rs.getString("PASSENGER_PHONE"));
        b.setPassengerIdCard(rs.getString("PASSENGER_ID_CARD"));
        Timestamp bookingTs = rs.getTimestamp("BOOKING_DATE");
        if (bookingTs != null) b.setBookingDate(bookingTs.toLocalDateTime());
        b.setTicketPrice(rs.getBigDecimal("TICKET_PRICE"));
        String status = rs.getString("STATUS");
        if (status != null) b.setStatus(BookingStatus.valueOf(status));
        Timestamp createdTs = rs.getTimestamp("CREATED_DATE");
        if (createdTs != null) b.setCreatedDate(createdTs.toLocalDateTime());
        Timestamp updatedTs = rs.getTimestamp("UPDATED_DATE");
        if (updatedTs != null) b.setUpdatedDate(updatedTs.toLocalDateTime());
        return b;
    }

    private static final String BASE_SELECT =
        "SELECT B.*, T.SEAT_NUMBER, T.PRICE AS TICKET_PRICE, F.ID AS FLIGHT_ID, " +
        "F.FLIGHT_CODE, F.DEPARTURE_AIRPORT, F.ARRIVAL_AIRPORT, F.DEPARTURE_TIME " +
        "FROM BOOKING B " +
        "JOIN TICKET T ON B.TICKET_ID = T.ID " +
        "JOIN FLIGHT F ON T.FLIGHT_ID = F.ID";

    public List<Booking> findAll() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY B.ID DESC";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Booking> search(Long flightId, BookingStatus status, String passengerName) throws SQLException {
        List<Booking> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1");
        if (flightId != null) sql.append(" AND F.ID=?");
        if (status != null) sql.append(" AND B.STATUS=?");
        if (passengerName != null && !passengerName.isEmpty()) sql.append(" AND UPPER(B.PASSENGER_NAME) LIKE UPPER(?)");
        sql.append(" ORDER BY B.ID DESC");

        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            if (flightId != null) ps.setLong(idx++, flightId);
            if (status != null) ps.setString(idx++, status.name());
            if (passengerName != null && !passengerName.isEmpty()) ps.setString(idx++, "%" + passengerName + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Booking> findById(Long id) throws SQLException {
        String sql = BASE_SELECT + " WHERE B.ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Booking> findByCode(String code) throws SQLException {
        String sql = BASE_SELECT + " WHERE B.BOOKING_CODE=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Booking save(Booking b) throws SQLException {
        String sql = "INSERT INTO BOOKING (BOOKING_CODE, TICKET_ID, PASSENGER_NAME, PASSENGER_EMAIL, " +
                "PASSENGER_PHONE, PASSENGER_ID_CARD, BOOKING_DATE, STATUS, CREATED_DATE, CREATED_BY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, new String[]{"ID"})) {
            ps.setString(1, b.getBookingCode());
            ps.setLong(2, b.getTicketId());
            ps.setString(3, b.getPassengerName());
            ps.setString(4, b.getPassengerEmail());
            ps.setString(5, b.getPassengerPhone());
            ps.setString(6, b.getPassengerIdCard());
            ps.setTimestamp(7, Timestamp.valueOf(b.getBookingDate() != null ? b.getBookingDate() : LocalDateTime.now()));
            ps.setString(8, b.getStatus() != null ? b.getStatus().name() : BookingStatus.PENDING.name());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            if (b.getCreatedBy() != null) ps.setLong(10, b.getCreatedBy());
            else ps.setNull(10, java.sql.Types.NUMERIC);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) b.setId(rs.getLong(1));
            }
        }
        return b;
    }

    public List<Booking> findByCreatedBy(Long userId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE B.CREATED_BY=? ORDER BY B.ID DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Booking> searchByUser(Long userId, BookingStatus status) throws SQLException {
        List<Booking> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE B.CREATED_BY=?");
        if (status != null) sql.append(" AND B.STATUS=?");
        sql.append(" ORDER BY B.ID DESC");
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            ps.setLong(1, userId);
            if (status != null) ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void update(Booking b) throws SQLException {
        String sql = "UPDATE BOOKING SET PASSENGER_NAME=?, PASSENGER_EMAIL=?, PASSENGER_PHONE=?, " +
                "PASSENGER_ID_CARD=?, STATUS=?, UPDATED_DATE=? WHERE ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, b.getPassengerName());
            ps.setString(2, b.getPassengerEmail());
            ps.setString(3, b.getPassengerPhone());
            ps.setString(4, b.getPassengerIdCard());
            ps.setString(5, b.getStatus().name());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(7, b.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Long id, BookingStatus status) throws SQLException {
        String sql = "UPDATE BOOKING SET STATUS=?, UPDATED_DATE=? WHERE ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM BOOKING WHERE ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existsByCode(String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM BOOKING WHERE BOOKING_CODE=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}
