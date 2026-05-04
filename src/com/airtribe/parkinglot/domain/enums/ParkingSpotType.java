package com.airtribe.parkinglot.domain.enums;

/**
 * Enum representing different types of parking spots based on size and vehicle compatibility.
 * Includes logic to determine if a specific vehicle type can fit into a spot type.
 */
public enum ParkingSpotType {
    SMALL("For motorcycles only") {
        @Override
        public boolean canFit(VehicleType vehicleType) {
            if (vehicleType == null) {
                return false;
            }
            return vehicleType == VehicleType.MOTORCYCLE;
        }
    },
    MEDIUM("For motorcycles, cars, and vans") {
        @Override
        public boolean canFit(VehicleType vehicleType) {
            if (vehicleType == null) {
                return false;
            }
            return vehicleType == VehicleType.MOTORCYCLE ||
                   vehicleType == VehicleType.CAR ||
                   vehicleType == VehicleType.VAN;
        }
    },
    LARGE("For all vehicle types including buses") {
        @Override
        public boolean canFit(VehicleType vehicleType) {
            // LARGE spots can fit all vehicle types
            return vehicleType != null;
        }
    };

    private final String description;

    ParkingSpotType(String description) {
        this.description = description;
    }

    /**
     * Determines whether a vehicle of the given type can fit into this parking spot.
     *
     * @param vehicleType the type of vehicle
     * @return true if the vehicle can fit, false otherwise
     */
    public abstract boolean canFit(VehicleType vehicleType);

    public String getDescription() {
        return description;
    }
}

