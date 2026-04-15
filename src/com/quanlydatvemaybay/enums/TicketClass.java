package com.quanlydatvemaybay.enums;

public enum TicketClass {
    ECONOMY("Phổ thông"),
    BUSINESS("Thương gia"),
    FIRST_CLASS("Hạng nhất");

    private final String displayName;

    TicketClass(String displayName) {
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
