package com.airtribe.parkinglot.exceptions;

/**
 * Exception thrown when no suitable parking spot is available for a vehicle.
 */
public class SpotNotFoundException extends ParkingLotException {
    public SpotNotFoundException(String vehicleType) {
        super("No available parking spot found for " + vehicleType);
    }

    public SpotNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

