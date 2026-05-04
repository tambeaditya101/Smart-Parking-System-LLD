package com.airtribe.parkinglot.domain.strategies;

import com.airtribe.parkinglot.domain.enums.VehicleType;
import com.airtribe.parkinglot.domain.models.ParkingSpot;

/**
 * Interface for different parking spot allocation strategies.
 * Implementing classes define how spots are selected for incoming vehicles.
 */
public interface SpotAllocationStrategy {

    /**
     * Finds an available parking spot suitable for the given vehicle type.
     *
     * @param vehicleType the type of vehicle to park
     * @return a suitable parking spot, or null if no spot is available
     */
    ParkingSpot findSpot(VehicleType vehicleType);

    /**
     * Returns the name of this strategy for display/logging purposes.
     *
     * @return the strategy name
     */
    String getStrategyName();
}

