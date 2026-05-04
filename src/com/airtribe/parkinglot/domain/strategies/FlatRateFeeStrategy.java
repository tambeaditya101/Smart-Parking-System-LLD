package com.airtribe.parkinglot.domain.strategies;

import com.airtribe.parkinglot.domain.enums.VehicleType;

/**
 * Flat rate fee calculation strategy.
 * Charges a fixed amount regardless of vehicle type or duration.
 * Fixed rate: $20 per parking session
 */
public class FlatRateFeeStrategy implements FeeCalculationStrategy {

    private static final double FLAT_RATE = 20.0;

    @Override
    public double calculateFee(long durationInMinutes, VehicleType vehicleType) {
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }
        if (durationInMinutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }

        // Flat rate: always returns $20 regardless of duration and vehicle type
        return FLAT_RATE;
    }

    @Override
    public String getStrategyName() {
        return "Flat Rate Fee Strategy";
    }

    @Override
    public String toString() {
        return "FlatRateFeeStrategy{" +
                "FLAT_RATE=" + FLAT_RATE +
                '}';
    }
}

