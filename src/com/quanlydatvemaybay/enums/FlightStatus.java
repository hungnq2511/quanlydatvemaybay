package com.quanlydatvemaybay.enums;

public enum FlightStatus {
    SCHEDULED("Đã lên lịch"),
    BOARDING("Đang lên máy bay"),
    DEPARTED("Đã khởi hành"),
    ARRIVED("Đã đến nơi"),
    DELAYED("Bị trễ"),
    CANCELLED("Đã hủy");

    private final String displayName;

    FlightStatus(String displayName) {
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
