package com.quanlydatvemaybay.enums;

public enum TicketStatus {
    AVAILABLE("Còn trống"),
    BOOKED("Đã đặt"),
    CANCELLED("Đã hủy");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
