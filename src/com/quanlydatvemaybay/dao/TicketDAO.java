package com.quanlydatvemaybay.dao;

import com.quanlydatvemaybay.config.DatabaseConfig;
import com.quanlydatvemaybay.entity.Ticket;
import com.quanlydatvemaybay.enums.TicketClass;
import com.quanlydatvemaybay.enums.TicketStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getInstance().getConnection();
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setId(rs.getLong("ID"));
        t.setFlightId(rs.getLong("FLIGHT_ID"));
        t.setFlightCode(rs.getString("FLIGHT_CODE"));
        t.setSeatNumber(rs.getString("SEAT_NUMBER"));
        String cls = rs.getString("TICKET_CLASS");
        if (cls != null) t.setTicketClass(TicketClass.valueOf(cls));
        t.setPrice(rs.getBigDecimal("PRICE"));
        String status = rs.getString("STATUS");
        if (status != null) t.setStatus(TicketStatus.valueOf(status));
        Timestamp createdTs = rs.getTimestamp("CREATED_DATE");
        if (createdTs != null) t.setCreatedDate(createdTs.toLocalDateTime());
        Timestamp updatedTs = rs.getTimestamp("UPDATED_DATE");
        if (updatedTs != null) t.setUpdatedDate(updatedTs.toLocalDateTime());
        return t;
    }

    public List<Ticket> findAll() throws SQLException {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT T.*, F.FLIGHT_CODE FROM TICKET T JOIN FLIGHT F ON T.FLIGHT_ID = F.ID ORDER BY T.ID DESC";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Ticket> findByFlightId(Long flightId) throws SQLException {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT T.*, F.FLIGHT_CODE FROM TICKET T JOIN FLIGHT F ON T.FLIGHT_ID = F.ID WHERE T.FLIGHT_ID=? ORDER BY T.ID DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, flightId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Ticket> findByFlightIdAndStatus(Long flightId, TicketStatus status) throws SQLException {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT T.*, F.FLIGHT_CODE FROM TICKET T JOIN FLIGHT F ON T.FLIGHT_ID = F.ID WHERE T.FLIGHT_ID=? AND T.STATUS=? ORDER BY T.ID DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, flightId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Ticket> findByStatus(TicketStatus status) throws SQLException {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT T.*, F.FLIGHT_CODE FROM TICKET T JOIN FLIGHT F ON T.FLIGHT_ID = F.ID WHERE T.STATUS=? ORDER BY T.ID DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Ticket> search(Long flightId, TicketStatus status) throws SQLException {
        if (flightId != null && status != null) return findByFlightIdAndStatus(flightId, status);
        if (flightId != null) return findByFlightId(flightId);
        if (status != null) return findByStatus(status);
        return findAll();
    }

    public Optional<Ticket> findById(Long id) throws SQLException {
        String sql = "SELECT T.*, F.FLIGHT_CODE FROM TICKET T JOIN FLIGHT F ON T.FLIGHT_ID = F.ID WHERE T.ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Ticket save(Ticket t) throws SQLException {
        String sql = "INSERT INTO TICKET (FLIGHT_ID, SEAT_NUMBER, TICKET_CLASS, PRICE, STATUS, CREATED_DATE) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, new String[]{"ID"})) {
            ps.setLong(1, t.getFlightId());
            ps.setString(2, t.getSeatNumber());
            ps.setString(3, t.getTicketClass().name());
            ps.setBigDecimal(4, t.getPrice());
            ps.setString(5, t.getStatus() != null ? t.getStatus().name() : TicketStatus.AVAILABLE.name());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getLong(1));
            }
        }
        return t;
    }

    public void update(Ticket t) throws SQLException {
        String sql = "UPDATE TICKET SET FLIGHT_ID=?, SEAT_NUMBER=?, TICKET_CLASS=?, PRICE=?, STATUS=?, UPDATED_DATE=? WHERE ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, t.getFlightId());
            ps.setString(2, t.getSeatNumber());
            ps.setString(3, t.getTicketClass().name());
            ps.setBigDecimal(4, t.getPrice());
            ps.setString(5, t.getStatus().name());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(7, t.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Long id, TicketStatus status) throws SQLException {
        String sql = "UPDATE TICKET SET STATUS=?, UPDATED_DATE=? WHERE ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM TICKET WHERE ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existsByFlightAndSeat(Long flightId, String seatNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TICKET WHERE FLIGHT_ID=? AND SEAT_NUMBER=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, flightId);
            ps.setString(2, seatNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean existsByFlightAndSeatExcludeId(Long flightId, String seatNumber, Long id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TICKET WHERE FLIGHT_ID=? AND SEAT_NUMBER=? AND ID != ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, flightId);
            ps.setString(2, seatNumber);
            ps.setLong(3, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean hasBooking(Long ticketId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM BOOKING WHERE TICKET_ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}
