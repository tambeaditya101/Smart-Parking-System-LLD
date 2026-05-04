package com.airtribe.parkinglot.domain.models;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton representing the entire parking lot.
 * A parking lot contains multiple floors, each with multiple parking spots.
 * Thread-safe implementation using double-checked locking pattern.
 */
public class ParkingLot {
    private static volatile ParkingLot instance;
    private final List<ParkingFloor> floors;
    private final String lotId;
    private final int totalCapacity;

    /**
     * Private constructor for singleton pattern.
     */
    private ParkingLot(String lotId) {
        this.lotId = lotId;
        this.floors = Collections.synchronizedList(new ArrayList<>());
        this.totalCapacity = 0;
    }

    /**
     * Gets the singleton instance of ParkingLot.
     * Uses double-checked locking for thread safety and performance.
     *
     * @return the singleton instance
     */
    public static ParkingLot getInstance() {
        if (instance == null) {
            synchronized (ParkingLot.class) {
                if (instance == null) {
                    instance = new ParkingLot("PARKING-LOT-001");
                }
            }
        }
        return instance;
    }

    /**
     * Resets the singleton instance (for testing purposes).
     */
    public static void resetInstance() {
        synchronized (ParkingLot.class) {
            instance = null;
        }
    }

    /**
     * Adds a parking floor to the lot.
     *
     * @param floor the parking floor to add
     * @return true if the floor was added, false if a floor with the same ID already exists
     */
    public boolean addFloor(ParkingFloor floor) {
        if (floor == null) {
            throw new IllegalArgumentException("Floor cannot be null");
        }
        // Check if floor with this ID already exists
        boolean exists = floors.stream()
                .anyMatch(f -> f.getFloorId().equals(floor.getFloorId()));
        if (exists) {
            return false;
        }
        return floors.add(floor);
    }

    /**
     * Gets a floor by its ID.
     *
     * @param floorId the ID of the floor
     * @return the floor if found, null otherwise
     */
    public ParkingFloor getFloor(String floorId) {
        return floors.stream()
                .filter(f -> f.getFloorId().equals(floorId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all floors in the parking lot.
     *
     * @return an unmodifiable list of floors
     */
    public List<ParkingFloor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    /**
     * Gets the total number of floors.
     *
     * @return the number of floors
     */
    public int getFloorCount() {
        return floors.size();
    }

    /**
     * Gets the total number of parking spots across all floors.
     *
     * @return the total capacity
     */
    public int getTotalCapacity() {
        return floors.stream()
                .mapToInt(ParkingFloor::getTotalSpots)
                .sum();
    }

    /**
     * Gets the total number of available spots across all floors.
     *
     * @return the number of available spots
     */
    public int getTotalAvailableSpots() {
        return floors.stream()
                .mapToInt(ParkingFloor::getAvailableSpotCount)
                .sum();
    }

    /**
     * Gets the total number of occupied spots across all floors.
     *
     * @return the number of occupied spots
     */
    public int getTotalOccupiedSpots() {
        return getTotalCapacity() - getTotalAvailableSpots();
    }

    /**
     * Gets the occupancy rate as a percentage.
     *
     * @return the occupancy percentage (0-100)
     */
    public double getOccupancyRate() {
        int total = getTotalCapacity();
        if (total == 0) {
            return 0.0;
        }
        return (getTotalOccupiedSpots() * 100.0) / total;
    }

    public String getLotId() {
        return lotId;
    }

    @Override
    public String toString() {
        return "ParkingLot{" +
                "lotId='" + lotId + '\'' +
                ", floors=" + getFloorCount() +
                ", totalCapacity=" + getTotalCapacity() +
                ", available=" + getTotalAvailableSpots() +
                ", occupied=" + getTotalOccupiedSpots() +
                ", occupancyRate=" + String.format("%.2f", getOccupancyRate()) + "%" +
                '}';
    }
}

