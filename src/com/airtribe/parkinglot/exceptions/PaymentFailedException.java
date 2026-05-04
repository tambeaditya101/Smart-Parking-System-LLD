package com.airtribe.parkinglot.exceptions;

/**
 * Exception thrown when payment processing fails.
 */
public class PaymentFailedException extends ParkingLotException {
    public PaymentFailedException(String message) {
        super(message);
    }

    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

