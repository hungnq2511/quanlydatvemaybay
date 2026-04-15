package com.quanlydatvemaybay.entity;

import com.quanlydatvemaybay.enums.TicketClass;
import com.quanlydatvemaybay.enums.TicketStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ticket {
    private Long id;
    private Long flightId;
    private String flightCode;
    private String seatNumber;
    private TicketClass ticketClass;
    private BigDecimal price;
    private TicketStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Ticket() {}

    public Ticket(Long id, Long flightId, String flightCode, String seatNumber,
                  TicketClass ticketClass, BigDecimal price, TicketStatus status) {
        this.id = id;
        this.flightId = flightId;
        this.flightCode = flightCode;
        this.seatNumber = seatNumber;
        this.ticketClass = ticketClass;
        this.price = price;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }

    public String getFlightCode() { return flightCode; }
    public void setFlightCode(String flightCode) { this.flightCode = flightCode; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public TicketClass getTicketClass() { return ticketClass; }
    public void setTicketClass(TicketClass ticketClass) { this.ticketClass = ticketClass; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    @Override
    public String toString() {
        return seatNumber + " - " + (flightCode != null ? flightCode : "") + " [" + (ticketClass != null ? ticketClass.getDisplayName() : "") + "]";
    }
}
