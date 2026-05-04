package com.airtribe.parkinglot.domain.models;

import com.airtribe.parkinglot.domain.enums.ParkingSpotType;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a single parking spot in the parking lot.
 * Each spot is identified by a unique ID, has a type, and tracks its availability status in a thread-safe manner.
 */
public class ParkingSpot {
    private final String spotId;
    private final ParkingSpotType spotType;
    private final AtomicBoolean isAvailable;
    private Vehicle parkedVehicle;

    public ParkingSpot(String spotId, ParkingSpotType spotType) {
        if (spotId == null || spotId.trim().isEmpty()) {
            throw new IllegalArgumentException("Spot ID cannot be null or empty");
        }
        if (spotType == null) {
            throw new IllegalArgumentException("Spot type cannot be null");
        }
        this.spotId = spotId;
        this.spotType = spotType;
        this.isAvailable = new AtomicBoolean(true);
        this.parkedVehicle = null;
    }

    /**
     * Attempts to park a vehicle in this spot.
     * This operation is atomic and thread-safe.
     *
     * @param vehicle the vehicle to park
     * @return true if the vehicle was successfully parked, false if the spot is unavailable or vehicle cannot fit
     */
    public synchronized boolean parkVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }

        // Check if spot is available and vehicle can fit
        if (!isAvailable.get() || !spotType.canFit(vehicle.getVehicleType())) {
            return false;
        }

        // Atomically transition from available to unavailable
        if (isAvailable.compareAndSet(true, false)) {
            this.parkedVehicle = vehicle;
            return true;
        }
        return false;
    }

    /**
     * Attempts to unpark the vehicle from this spot.
     * Returns the vehicle if it was parked and belongs to the spot.
     *
     * @return the vehicle that was parked, or null if the spot was already available
     */
    public synchronized Vehicle unparkVehicle() {
        if (!isAvailable.get()) {
            Vehicle vehicle = parkedVehicle;
            this.parkedVehicle = null;
            isAvailable.set(true);
            return vehicle;
        }
        return null;
    }

    /**
     * Checks if the spot is currently available.
     *
     * @return true if available, false if occupied
     */
    public boolean isAvailable() {
        return isAvailable.get();
    }

    /**
     * Gets the vehicle currently parked in this spot, if any.
     *
     * @return the parked vehicle, or null if the spot is empty
     */
    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    public String getSpotId() {
        return spotId;
    }

    public ParkingSpotType getSpotType() {
        return spotType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSpot that = (ParkingSpot) o;
        return Objects.equals(spotId, that.spotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spotId);
    }

    @Override
    public String toString() {
        return "ParkingSpot{" +
                "spotId='" + spotId + '\'' +
                ", spotType=" + spotType +
                ", isAvailable=" + isAvailable.get() +
                ", parkedVehicle=" + parkedVehicle +
                '}';
    }
}

