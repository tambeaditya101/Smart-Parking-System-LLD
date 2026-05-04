package com.airtribe.parkinglot.domain.strategies;

import com.airtribe.parkinglot.domain.enums.VehicleType;

/**
 * Interface for different parking fee calculation strategies.
 * Implementing classes define how fees are calculated based on duration and vehicle type.
 */
public interface FeeCalculationStrategy {

    /**
     * Calculates the parking fee based on the duration of parking and vehicle type.
     *
     * @param durationInMinutes the duration of parking in minutes
     * @param vehicleType the type of vehicle parked
     * @return the calculated fee amount
     */
    double calculateFee(long durationInMinutes, VehicleType vehicleType);

    /**
     * Returns the name of this strategy for display/logging purposes.
     *
     * @return the strategy name
     */
    String getStrategyName();
}

