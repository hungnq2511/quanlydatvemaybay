package com.quanlydatvemaybay.service;

import com.quanlydatvemaybay.config.DatabaseConfig;
import com.quanlydatvemaybay.dao.BookingDAO;
import com.quanlydatvemaybay.dao.FlightDAO;
import com.quanlydatvemaybay.dao.TicketDAO;
import com.quanlydatvemaybay.entity.Booking;
import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.entity.Ticket;
import com.quanlydatvemaybay.entity.User;
import com.quanlydatvemaybay.enums.BookingStatus;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.enums.TicketStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final FlightDAO flightDAO = new FlightDAO();
    private final EmailService emailService = new EmailService();

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

    /**
     * FIX #1, #7: Đặt vé an toàn với transaction + SELECT FOR UPDATE.
     *  - Khóa TICKET và FLIGHT trước khi check + update để chặn race condition.
     *  - Validate chuyến bay còn SCHEDULED và giờ khởi hành ở tương lai.
     *  - Email gửi SAU khi commit; lỗi SMTP không làm rollback.
     */
    public Booking create(Long ticketId, String passengerName, String passengerEmail,
                          String passengerPhone, String passengerIdCard) throws SQLException {
        Booking saved;
        try (Connection conn = DatabaseConfig.getInstance().newConnection()) {
            conn.setAutoCommit(false);
            try {
                Ticket ticket = ticketDAO.findByIdForUpdate(conn, ticketId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé ID=" + ticketId));

                if (ticket.getStatus() != TicketStatus.AVAILABLE) {
                    throw new IllegalArgumentException("Vé này không còn trống! Trạng thái: "
                            + ticket.getStatus().getDisplayName());
                }

                Flight flight = flightDAO.findByIdForUpdate(conn, ticket.getFlightId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến bay!"));

                validateFlightBookable(flight);

                if (flight.getAvailableSeats() <= 0) {
                    throw new IllegalArgumentException("Chuyến bay đã hết ghế!");
                }

                String bookingCode = generateBookingCode(conn);

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

                bookingDAO.save(conn, booking);
                ticketDAO.updateStatus(conn, ticketId, TicketStatus.BOOKED);
                flightDAO.updateAvailableSeats(conn, flight.getId(), flight.getAvailableSeats() - 1);

                conn.commit();
                saved = bookingDAO.findById(booking.getId()).orElse(booking);
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        // Gửi email NGOÀI transaction; lỗi SMTP không ảnh hưởng booking
        try {
            emailService.sendBookingConfirmation(saved);
        } catch (Exception ex) {
            System.err.println("[WARN] Gửi email xác nhận thất bại: " + ex.getMessage());
        }
        return saved;
    }

    /**
     * FIX #4: Update booking KHÔNG cho phép chuyển sang CANCELLED ở đây
     * (phải dùng cancel() để đồng bộ ticket/flight).
     */
    public Booking update(Long id, String passengerName, String passengerEmail,
                          String passengerPhone, String passengerIdCard, BookingStatus status) throws SQLException {
        Booking existing = bookingDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt vé ID=" + id));

        if (existing.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Không thể cập nhật đặt vé đã hủy!");
        }

        if (status != null && status == BookingStatus.CANCELLED && existing.getStatus() != BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Để hủy vé, vui lòng dùng chức năng \"Hủy vé\" (giúp hoàn ghế tự động).");
        }

        existing.setPassengerName(passengerName);
        existing.setPassengerEmail(passengerEmail);
        existing.setPassengerPhone(passengerPhone);
        existing.setPassengerIdCard(passengerIdCard);
        if (status != null) existing.setStatus(status);

        bookingDAO.update(existing);
        return bookingDAO.findById(id).orElse(existing);
    }

    /**
     * FIX #4: updateStatus cũng chặn đổi sang CANCELLED trực tiếp.
     */
    public void updateStatus(Long id, BookingStatus status) throws SQLException {
        Booking existing = bookingDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt vé ID=" + id));

        if (status == BookingStatus.CANCELLED && existing.getStatus() != BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Để hủy vé, vui lòng dùng chức năng \"Hủy vé\" (giúp hoàn ghế tự động).");
        }
        bookingDAO.updateStatus(id, status);
    }

    /**
     * FIX #3: Hủy vé idempotent + transaction.
     *  - Chỉ hoàn ghế khi ticket đang BOOKED (tránh double-refund).
     *  - Khóa TICKET + FLIGHT để chặn race.
     *  - User chỉ được hủy vé của chính mình.
     *  - Không cho hủy nếu chuyến bay đã khởi hành.
     */
    public void cancel(Long id) throws SQLException {
        User currentUser = AuthService.getCurrentUser();
        try (Connection conn = DatabaseConfig.getInstance().newConnection()) {
            conn.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.findById(conn, id)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt vé ID=" + id));

                // Permission: user chỉ được hủy booking của chính mình
                if (currentUser != null && !currentUser.isAdmin()
                        && !currentUser.getId().equals(booking.getCreatedBy())) {
                    throw new IllegalArgumentException("Bạn không có quyền hủy đặt vé này!");
                }

                if (booking.getStatus() == BookingStatus.CANCELLED) {
                    throw new IllegalArgumentException("Đặt vé này đã được hủy trước đó!");
                }
                if (booking.getStatus() == BookingStatus.COMPLETED) {
                    throw new IllegalArgumentException("Không thể hủy vé đã hoàn thành chuyến bay!");
                }

                // Không cho hủy nếu chuyến bay đã khởi hành
                if (booking.getDepartureTime() != null
                        && booking.getDepartureTime().isBefore(LocalDateTime.now())) {
                    throw new IllegalArgumentException("Chuyến bay đã khởi hành, không thể hủy vé!");
                }

                Ticket ticket = ticketDAO.findByIdForUpdate(conn, booking.getTicketId()).orElse(null);

                bookingDAO.updateStatus(conn, id, BookingStatus.CANCELLED);

                if (ticket != null && ticket.getStatus() == TicketStatus.BOOKED) {
                    ticketDAO.updateStatus(conn, ticket.getId(), TicketStatus.AVAILABLE);

                    Flight flight = flightDAO.findByIdForUpdate(conn, ticket.getFlightId()).orElse(null);
                    if (flight != null) {
                        int newAvailable = flight.getAvailableSeats() + 1;
                        if (newAvailable > flight.getTotalSeats()) newAvailable = flight.getTotalSeats();
                        flightDAO.updateAvailableSeats(conn, flight.getId(), newAvailable);
                    }
                }
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                try { conn.rollback(); } catch (SQLException ignore) {}
                throw ex;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * FIX #8: Xóa booking phải hoàn ghế + trả ticket về AVAILABLE
     * (chỉ admin được phép, panel UI đã kiểm).
     */
    public void delete(Long id) throws SQLException {
        try (Connection conn = DatabaseConfig.getInstance().newConnection()) {
            conn.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt vé ID=" + id));

                Ticket ticket = ticketDAO.findByIdForUpdate(conn, booking.getTicketId()).orElse(null);

                // Hoàn ghế nếu ticket vẫn đang BOOKED (booking chưa CANCELLED)
                if (ticket != null && ticket.getStatus() == TicketStatus.BOOKED
                        && booking.getStatus() != BookingStatus.CANCELLED) {
                    ticketDAO.updateStatus(conn, ticket.getId(), TicketStatus.AVAILABLE);
                    Flight flight = flightDAO.findByIdForUpdate(conn, ticket.getFlightId()).orElse(null);
                    if (flight != null) {
                        int newAvailable = flight.getAvailableSeats() + 1;
                        if (newAvailable > flight.getTotalSeats()) newAvailable = flight.getTotalSeats();
                        flightDAO.updateAvailableSeats(conn, flight.getId(), newAvailable);
                    }
                }
                bookingDAO.delete(id);
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * FIX #7: Validate chuyến bay đủ điều kiện để đặt vé.
     */
    private void validateFlightBookable(Flight flight) {
        FlightStatus s = flight.getStatus();
        if (s == FlightStatus.CANCELLED) {
            throw new IllegalArgumentException("Chuyến bay đã bị hủy, không thể đặt vé!");
        }
        if (s == FlightStatus.DEPARTED || s == FlightStatus.ARRIVED) {
            throw new IllegalArgumentException("Chuyến bay đã khởi hành, không thể đặt vé!");
        }
        if (flight.getDepartureTime() != null
                && flight.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Chuyến bay đã qua thời gian khởi hành!");
        }
    }

    private String generateBookingCode(Connection conn) throws SQLException {
        String code;
        do {
            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            code = "BK" + uuid.substring(0, 8);
        } while (bookingDAO.existsByCode(conn, code));
        return code;
    }
}
