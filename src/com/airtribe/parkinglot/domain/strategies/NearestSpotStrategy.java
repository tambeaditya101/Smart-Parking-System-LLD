package com.airtribe.parkinglot.domain.strategies;

import com.airtribe.parkinglot.domain.enums.VehicleType;
import com.airtribe.parkinglot.domain.models.ParkingFloor;
import com.airtribe.parkinglot.domain.models.ParkingLot;
import com.airtribe.parkinglot.domain.models.ParkingSpot;
import java.util.List;

/**
 * Nearest spot allocation strategy.
 * Searches floors sequentially (starting from Floor 1) and returns the first available spot
 * that can accommodate the vehicle type.
 */
public class NearestSpotStrategy implements SpotAllocationStrategy {

    @Override
    public ParkingSpot findSpot(VehicleType vehicleType) {
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }

        ParkingLot lot = ParkingLot.getInstance();
        List<ParkingFloor> floors = lot.getFloors();

        // Search floors in order
        for (ParkingFloor floor : floors) {
            List<ParkingSpot> availableSpots = floor.getAvailableSpots();
            for (ParkingSpot spot : availableSpots) {
                // Return the first spot that can fit this vehicle
                if (spot.getSpotType().canFit(vehicleType)) {
                    return spot;
                }
            }
        }

        // No available spot found
        return null;
    }

    @Override
    public String getStrategyName() {
        return "Nearest Spot Strategy";
    }

    @Override
    public String toString() {
        return "NearestSpotStrategy{" +
                "searchOrder='Floor-by-floor sequential'" +
                '}';
    }
}

