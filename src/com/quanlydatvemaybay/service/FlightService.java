package com.quanlydatvemaybay.service;

import com.quanlydatvemaybay.dao.FlightDAO;
import com.quanlydatvemaybay.dao.TicketDAO;
import com.quanlydatvemaybay.entity.Flight;
import com.quanlydatvemaybay.enums.FlightStatus;
import com.quanlydatvemaybay.enums.TicketStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlightService {

    private final FlightDAO flightDAO = new FlightDAO();
    private final TicketDAO ticketDAO = new TicketDAO();

    public List<Flight> getAll() throws SQLException {
        return flightDAO.findAll();
    }

    public List<Flight> search(String departure, String arrival, String airline) throws SQLException {
        return flightDAO.search(departure, arrival, airline);
    }

    public Optional<Flight> getById(Long id) throws SQLException {
        return flightDAO.findById(id);
    }

    public Flight create(Flight flight) throws SQLException {
        if (flightDAO.existsByCode(flight.getFlightCode())) {
            throw new IllegalArgumentException("Mã chuyến bay '" + flight.getFlightCode() + "' đã tồn tại!");
        }
        if (flight.getDepartureTime() != null && flight.getArrivalTime() != null
                && !flight.getDepartureTime().isBefore(flight.getArrivalTime())) {
            throw new IllegalArgumentException("Thời gian khởi hành phải trước thời gian đến!");
        }
        // FIX #7: chặn tạo chuyến bay quá khứ
        if (flight.getDepartureTime() != null
                && flight.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian khởi hành phải ở tương lai!");
        }
        if (flight.getStatus() == null) flight.setStatus(FlightStatus.SCHEDULED);
        return flightDAO.save(flight);
    }

    /**
     * Tìm chuyến bay thông minh: theo điểm đi/đến và ngày khởi hành (± dayRange).
     * Trả về danh sách sắp xếp theo khoảng cách thời gian gần với ngày yêu cầu.
     */
    public List<Flight> searchSmart(String departure, String arrival,
                                    LocalDateTime targetDate, int dayRangeBefore, int dayRangeAfter) throws SQLException {
        List<Flight> list = flightDAO.searchSmart(departure, arrival, targetDate, dayRangeBefore, dayRangeAfter);
        if (targetDate != null) {
            final LocalDateTime ref = targetDate;
            list = list.stream()
                    .sorted(Comparator.comparingLong(f -> Math.abs(
                            java.time.Duration.between(ref, f.getDepartureTime()).toMinutes())))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public Flight update(Long id, Flight flight) throws SQLException {
        Flight existing = flightDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến bay ID=" + id));

        if (flightDAO.existsByCodeExcludeId(flight.getFlightCode(), id)) {
            throw new IllegalArgumentException("Mã chuyến bay '" + flight.getFlightCode() + "' đã tồn tại!");
        }
        if (flight.getDepartureTime() != null && flight.getArrivalTime() != null
                && !flight.getDepartureTime().isBefore(flight.getArrivalTime())) {
            throw new IllegalArgumentException("Thời gian khởi hành phải trước thời gian đến!");
        }

        existing.setFlightCode(flight.getFlightCode());
        existing.setAirline(flight.getAirline());
        existing.setDepartureAirport(flight.getDepartureAirport());
        existing.setArrivalAirport(flight.getArrivalAirport());
        existing.setDepartureTime(flight.getDepartureTime());
        existing.setArrivalTime(flight.getArrivalTime());
        existing.setTotalSeats(flight.getTotalSeats());
        existing.setAvailableSeats(flight.getAvailableSeats());
        existing.setPrice(flight.getPrice());
        existing.setStatus(flight.getStatus());

        flightDAO.update(existing);
        return existing;
    }

    public void updateStatus(Long id, FlightStatus status) throws SQLException {
        if (!flightDAO.findById(id).isPresent()) {
            throw new IllegalArgumentException("Không tìm thấy chuyến bay ID=" + id);
        }
        flightDAO.updateStatus(id, status);
    }

    public void delete(Long id) throws SQLException {
        Flight flight = flightDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến bay ID=" + id));

        long bookedCount = ticketDAO.findByFlightIdAndStatus(id, TicketStatus.BOOKED).size();
        if (bookedCount > 0) {
            throw new IllegalArgumentException("Không thể xóa chuyến bay đã có vé được đặt!");
        }
        flightDAO.delete(id);
    }
}
