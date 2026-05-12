package com.ticketsystem.model.enums;

public enum TicketStatus {
    NEW("Nowe"),
    IN_PROGRESS("W realizacji"),
    DELAYED("Opóźnione"),
    COMPLETED("Zakończone"),
    CANCELLED("Anulowane");

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

    /**
     * Returns TicketStatus from ordinal number (used in CLI selection menus).
     */
    public static TicketStatus fromOrdinal(int index) {
        TicketStatus[] values = values();
        if (index < 1 || index > values.length) {
            throw new IllegalArgumentException("Nieprawidłowy numer statusu: " + index);
        }
        return values[index - 1];
    }
}
