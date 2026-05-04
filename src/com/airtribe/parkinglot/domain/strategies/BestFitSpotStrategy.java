package com.airtribe.parkinglot.domain.strategies;

import com.airtribe.parkinglot.domain.enums.ParkingSpotType;
import com.airtribe.parkinglot.domain.enums.VehicleType;
import com.airtribe.parkinglot.domain.models.ParkingFloor;
import com.airtribe.parkinglot.domain.models.ParkingLot;
import com.airtribe.parkinglot.domain.models.ParkingSpot;
import java.util.List;

/**
 * Best fit allocation strategy.
 * Allocates the smallest spot type that can fit the vehicle.
 * This maximizes the availability of larger spots for vehicles that need them.
 *
 * Priority order:
 * 1. SMALL (if vehicle can fit)
 * 2. MEDIUM (if vehicle can fit)
 * 3. LARGE (as fallback)
 */
public class BestFitSpotStrategy implements SpotAllocationStrategy {

    @Override
    public ParkingSpot findSpot(VehicleType vehicleType) {
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }

        ParkingLot lot = ParkingLot.getInstance();
        List<ParkingFloor> floors = lot.getFloors();

        // Try to find the smallest spot type that fits
        for (ParkingSpotType spotType : new ParkingSpotType[]{
                ParkingSpotType.SMALL,
                ParkingSpotType.MEDIUM,
                ParkingSpotType.LARGE
        }) {
            if (spotType.canFit(vehicleType)) {
                // Search all floors for this spot type
                for (ParkingFloor floor : floors) {
                    List<ParkingSpot> availableSpots = floor.getAvailableSpotsByType(spotType);
                    if (!availableSpots.isEmpty()) {
                        return availableSpots.get(0);
                    }
                }
            }
        }

        // No available spot found
        return null;
    }

    @Override
    public String getStrategyName() {
        return "Best Fit Spot Strategy";
    }

    @Override
    public String toString() {
        return "BestFitSpotStrategy{" +
                "priority='SMALL → MEDIUM → LARGE'" +
                '}';
    }
}

