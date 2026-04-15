package com.quanlydatvemaybay.service;

import com.quanlydatvemaybay.dao.FlightDAO;
import com.quanlydatvemaybay.dao.TicketDAO;
import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.entity.Ticket;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.enums.TicketClass;
import com.quanlydatvemaybay.enums.TicketStatus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketService {

    private final TicketDAO ticketDAO = new TicketDAO();
    private final FlightDAO flightDAO = new FlightDAO();

    public List<Ticket> getAll() throws SQLException {
        return ticketDAO.findAll();
    }

    public List<Ticket> search(Long flightId, TicketStatus status) throws SQLException {
        return ticketDAO.search(flightId, status);
    }

    public Optional<Ticket> getById(Long id) throws SQLException {
        return ticketDAO.findById(id);
    }

    private void validateFlightForTicket(Flight flight) {
        FlightStatus status = flight.getStatus();
        if (status == FlightStatus.CANCELLED) {
            throw new IllegalArgumentException("Chuyến bay đã hủy, không thể tạo vé!");
        }
        if (status == FlightStatus.ARRIVED) {
            throw new IllegalArgumentException("Chuyến bay đã đến nơi, không thể tạo vé!");
        }
        if (status == FlightStatus.DEPARTED) {
            throw new IllegalArgumentException("Chuyến bay đã khởi hành, không thể tạo vé!");
        }
    }

    public Ticket create(Long flightId, String seatNumber, TicketClass ticketClass, BigDecimal price) throws SQLException {
        Flight flight = flightDAO.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến bay ID=" + flightId));

        validateFlightForTicket(flight);

        if (ticketDAO.existsByFlightAndSeat(flightId, seatNumber)) {
            throw new IllegalArgumentException("Ghế '" + seatNumber + "' đã tồn tại trên chuyến bay này!");
        }

        Ticket ticket = new Ticket();
        ticket.setFlightId(flightId);
        ticket.setFlightCode(flight.getFlightCode());
        ticket.setSeatNumber(seatNumber);
        ticket.setTicketClass(ticketClass);
        ticket.setPrice(price);
        ticket.setStatus(TicketStatus.AVAILABLE);

        return ticketDAO.save(ticket);
    }

    public List<Ticket> createBulk(Long flightId, int count, TicketClass ticketClass, BigDecimal price, String seatPrefix) throws SQLException {
        if (count > 500) throw new IllegalArgumentException("Số lượng tối đa là 500 vé!");
        if (seatPrefix == null || seatPrefix.isEmpty()) seatPrefix = "S";

        Flight flight = flightDAO.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến bay ID=" + flightId));

        validateFlightForTicket(flight);

        List<Ticket> result = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String seatNumber = seatPrefix + "-" + String.format("%03d", i);
            if (ticketDAO.existsByFlightAndSeat(flightId, seatNumber)) continue;

            Ticket ticket = new Ticket();
            ticket.setFlightId(flightId);
            ticket.setFlightCode(flight.getFlightCode());
            ticket.setSeatNumber(seatNumber);
            ticket.setTicketClass(ticketClass);
            ticket.setPrice(price);
            ticket.setStatus(TicketStatus.AVAILABLE);
            result.add(ticketDAO.save(ticket));
        }
        return result;
    }

    public Ticket update(Long id, Long flightId, String seatNumber, TicketClass ticketClass, BigDecimal price) throws SQLException {
        Ticket existing = ticketDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé ID=" + id));

        if (existing.getStatus() == TicketStatus.BOOKED) {
            throw new IllegalArgumentException("Không thể cập nhật vé đã được đặt!");
        }

        Flight flight = flightDAO.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến bay ID=" + flightId));

        if (ticketDAO.existsByFlightAndSeatExcludeId(flightId, seatNumber, id)) {
            throw new IllegalArgumentException("Ghế '" + seatNumber + "' đã tồn tại trên chuyến bay này!");
        }

        existing.setFlightId(flightId);
        existing.setFlightCode(flight.getFlightCode());
        existing.setSeatNumber(seatNumber);
        existing.setTicketClass(ticketClass);
        existing.setPrice(price);

        ticketDAO.update(existing);
        return existing;
    }

    public void updateStatus(Long id, TicketStatus status) throws SQLException {
        if (!ticketDAO.findById(id).isPresent()) {
            throw new IllegalArgumentException("Không tìm thấy vé ID=" + id);
        }
        ticketDAO.updateStatus(id, status);
    }

    public void delete(Long id) throws SQLException {
        if (!ticketDAO.findById(id).isPresent()) {
            throw new IllegalArgumentException("Không tìm thấy vé ID=" + id);
        }
        if (ticketDAO.hasBooking(id)) {
            throw new IllegalArgumentException("Không thể xóa vé đã có đặt chỗ!");
        }
        ticketDAO.delete(id);
    }
}
