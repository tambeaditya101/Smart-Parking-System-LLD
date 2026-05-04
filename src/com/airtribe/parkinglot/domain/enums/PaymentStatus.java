package com.airtribe.parkinglot.domain.enums;

/**
 * Enum representing the status of a payment transaction.
 */
public enum PaymentStatus {
    PENDING("Payment is pending"),
    COMPLETED("Payment has been completed successfully"),
    FAILED("Payment has failed"),
    REFUNDED("Payment has been refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

