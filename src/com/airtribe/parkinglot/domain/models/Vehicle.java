package com.airtribe.parkinglot.domain.models;

import com.airtribe.parkinglot.domain.enums.VehicleType;
import java.util.Objects;

/**
 * Represents a vehicle that can park in the parking lot.
 * Each vehicle has a unique license plate and type.
 */
public class Vehicle {
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final String ownerName;

    public Vehicle(String licensePlate, VehicleType vehicleType, String ownerName) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.ownerName = ownerName != null ? ownerName : "Unknown";
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(licensePlate, vehicle.licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licensePlate);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "licensePlate='" + licensePlate + '\'' +
                ", vehicleType=" + vehicleType +
                ", ownerName='" + ownerName + '\'' +
                '}';
    }
}

