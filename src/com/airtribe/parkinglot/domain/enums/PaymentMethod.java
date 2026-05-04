package com.airtribe.parkinglot.domain.enums;

/**
 * Enum representing different payment methods.
 */
public enum PaymentMethod {
    CREDIT_CARD("Payment via credit card"),
    DEBIT_CARD("Payment via debit card"),
    CASH("Payment via cash"),
    DIGITAL_WALLET("Payment via digital wallet"),
    UPI("Payment via UPI");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

