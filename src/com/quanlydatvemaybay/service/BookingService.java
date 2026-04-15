package com.quanlydatvemaybay.service;

import com.quanlydatvemaybay.dao.BookingDAO;
import com.quanlydatvemaybay.dao.FlightDAO;
import com.quanlydatvemaybay.dao.TicketDAO;
import com.quanlydatvemaybay.entity.Booking;
import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.entity.Ticket;
import com.quanlydatvemaybay.entity.User;
import com.quanlydatvemaybay.enums.BookingStatus;
import com.quanlydatvemaybay.enums.TicketStatus;

import com.quanlydatvemaybay.service.AuthService;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final FlightDAO flightDAO = new FlightDAO();

    public List<Booking> getAll() throws SQLException {
        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null && !currentUser.isAdmin()) {
            return bookingDAO.findByCreatedBy(currentUser.getId());
        }
        return bookingDAO.findAll();
    }

    public List<Booking> search(Long flightId, BookingStatus status, String passengerName) throws SQLException {
        User currentUser = AuthService.getCurrentUser();
if (currentUser != null && !currentUser.isAdmin()) {
            return bookingDAO.searchByUser(currentUser.getId(), status);
        }
        return bookingDAO.search(flightId, status, passengerName);
    }

    public Optional<Booking> getById(Long id) throws SQLException {
        Optional<Booking> opt = bookingDAO.findById(id);
        User currentUser = AuthService.getCurrentUser();
        if (opt.isPresent() && currentUser != null && !currentUser.isAdmin()) {
            Booking b = opt.get();
            if (!currentUser.getId().equals(b.getCreatedBy())) return Optional.empty();
        }
        return opt;
    }

    public Optional<Booking> getByCode(String code) throws SQLException {
        return bookingDAO.findByCode(code);
    }

    public Booking create(Long ticketId, String passengerName, String passengerEmail,
                          String passengerPhone, String passengerIdCard) throws SQLException {
        Ticket ticket = ticketDAO.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé ID=" + ticketId));

        if (ticket.getStatus() != TicketStatus.AVAILABLE) {
            throw new IllegalArgumentException("Vé này không còn trống! Trạng thái: " + ticket.getStatus().getDisplayName());
        }

        Flight flight = flightDAO.findById(ticket.getFlightId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến bay!"));

        String bookingCode = generateBookingCode();

        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setTicketId(ticketId);
        booking.setPassengerName(passengerName);
        booking.setPassengerEmail(passengerEmail);
        booking.setPassengerPhone(passengerPhone);
        booking.setPassengerIdCard(passengerIdCard);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);
        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null) booking.setCreatedBy(currentUser.getId());

        bookingDAO.save(booking);

        ticketDAO.updateStatus(ticketId, TicketStatus.BOOKED);

        int newAvailable = flight.getAvailableSeats() - 1;
        if (newAvailable < 0) newAvailable = 0;
        flightDAO.updateAvailableSeats(flight.getId(), newAvailable);

        return bookingDAO.findById(booking.getId()).orElse(booking);
    }

    public Booking update(Long id, String passengerName, String passengerEmail,
                          String passengerPhone, String passengerIdCard, BookingStatus status) throws SQLException {
        Booking existing = bookingDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt vé ID=" + id));

        if (existing.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Không thể cập nhật đặt vé đã hủy!");
        }

        existing.setPassengerName(passengerName);
        existing.setPassengerEmail(passengerEmail);
        existing.setPassengerPhone(passengerPhone);
        existing.setPassengerIdCard(passengerIdCard);
        if (status != null) existing.setStatus(status);

        bookingDAO.update(existing);
        return bookingDAO.findById(id).orElse(existing);
    }

    public void updateStatus(Long id, BookingStatus status) throws SQLException {
        if (!bookingDAO.findById(id).isPresent()) {
            throw new IllegalArgumentException("Không tìm thấy đặt vé ID=" + id);
        }
        bookingDAO.updateStatus(id, status);
    }

    public void cancel(Long id) throws SQLException {
        Booking booking = bookingDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt vé ID=" + id));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Đặt vé này đã được hủy trước đó!");
        }

        bookingDAO.updateStatus(id, BookingStatus.CANCELLED);

        Ticket ticket = ticketDAO.findById(booking.getTicketId()).orElse(null);
        if (ticket != null) {
            ticketDAO.updateStatus(ticket.getId(), TicketStatus.AVAILABLE);

            Flight flight = flightDAO.findById(ticket.getFlightId()).orElse(null);
            if (flight != null) {
                flightDAO.updateAvailableSeats(flight.getId(), flight.getAvailableSeats() + 1);
            }
        }
    }

    private String generateBookingCode() throws SQLException {
        String code;
        do {
            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            code = "BK" + uuid.substring(0, 8);
        } while (bookingDAO.existsByCode(code));
        return code;
    }
}
