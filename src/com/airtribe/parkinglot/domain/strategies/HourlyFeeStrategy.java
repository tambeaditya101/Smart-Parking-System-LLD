package com.airtribe.parkinglot.domain.strategies;

import com.airtribe.parkinglot.domain.enums.VehicleType;

/**
 * Hourly fee calculation strategy.
 * Charges different rates per hour based on vehicle type.
 *
 * Rates:
 * - MOTORCYCLE: $5/hr
 * - CAR: $10/hr
 * - VAN: $15/hr
 * - BUS: $20/hr
 */
public class HourlyFeeStrategy implements FeeCalculationStrategy {

    private static final double MOTORCYCLE_RATE = 5.0;
    private static final double CAR_RATE = 10.0;
    private static final double VAN_RATE = 15.0;
    private static final double BUS_RATE = 20.0;

    @Override
    public double calculateFee(long durationInMinutes, VehicleType vehicleType) {
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }
        if (durationInMinutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }

        // Convert minutes to hours and round up (minimum 1 hour charge)
        long hours = (durationInMinutes + 59) / 60;
        if (hours == 0) {
            hours = 1; // Minimum 1 hour charge
        }

        double hourlyRate = switch (vehicleType) {
            case MOTORCYCLE -> MOTORCYCLE_RATE;
            case CAR -> CAR_RATE;
            case VAN -> VAN_RATE;
            case BUS -> BUS_RATE;
        };

        return hours * hourlyRate;
    }

    @Override
    public String getStrategyName() {
        return "Hourly Fee Strategy";
    }

    @Override
    public String toString() {
        return "HourlyFeeStrategy{" +
                "MOTORCYCLE=" + MOTORCYCLE_RATE + "/hr, " +
                "CAR=" + CAR_RATE + "/hr, " +
                "VAN=" + VAN_RATE + "/hr, " +
                "BUS=" + BUS_RATE + "/hr" +
                '}';
    }
}

