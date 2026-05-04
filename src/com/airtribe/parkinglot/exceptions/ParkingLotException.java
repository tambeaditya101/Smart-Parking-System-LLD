package com.airtribe.parkinglot.exceptions;

/**
 * Base exception for parking lot operations.
 * All parking lot specific exceptions should extend this class.
 */
public class ParkingLotException extends Exception {
    public ParkingLotException(String message) {
        super(message);
    }

    public ParkingLotException(String message, Throwable cause) {
        super(message, cause);
    }
}

