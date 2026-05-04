package com.airtribe.parkinglot.domain.enums;

/**
 * Enum representing the status of a parking ticket.
 */
public enum TicketStatus {
    ACTIVE("Ticket is active, vehicle is parked"),
    COMPLETED("Ticket is completed, vehicle has exited"),
    PAID("Ticket has been paid"),
    CANCELLED("Ticket was cancelled");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

