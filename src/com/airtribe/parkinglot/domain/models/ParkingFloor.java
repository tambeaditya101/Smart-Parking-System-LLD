package com.airtribe.parkinglot.domain.models;

import com.airtribe.parkinglot.domain.enums.ParkingSpotType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Represents a single floor in a parking lot.
 * A floor contains multiple parking spots and provides thread-safe access to them.
 */
public class ParkingFloor {
    private final String floorId;
    private final int totalSpots;
    private final ConcurrentHashMap<String, ParkingSpot> spotMap;
    private final AtomicInteger availableSpots;

    public ParkingFloor(String floorId, int totalSpots) {
        if (floorId == null || floorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Floor ID cannot be null or empty");
        }
        if (totalSpots <= 0) {
            throw new IllegalArgumentException("Total spots must be greater than 0");
        }
        this.floorId = floorId;
        this.totalSpots = totalSpots;
        this.spotMap = new ConcurrentHashMap<>();
        this.availableSpots = new AtomicInteger(0);
    }

    /**
     * Adds a parking spot to this floor.
     * The spot ID must be unique within the floor.
     *
     * @param spot the parking spot to add
     * @return true if the spot was added, false if a spot with the same ID already exists or floor is full
     */
    public boolean addParkingSpot(ParkingSpot spot) {
        if (spot == null) {
            throw new IllegalArgumentException("Parking spot cannot be null");
        }
        if (spotMap.size() >= totalSpots) {
            throw new IllegalStateException("Floor is at maximum capacity");
        }
        ParkingSpot existing = spotMap.putIfAbsent(spot.getSpotId(), spot);
        if (existing == null) {
            availableSpots.incrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * Gets a parking spot by its ID.
     *
     * @param spotId the ID of the spot
     * @return the spot if found, null otherwise
     */
    public ParkingSpot getSpot(String spotId) {
        return spotMap.get(spotId);
    }

    /**
     * Gets all available spots on this floor.
     *
     * @return a list of available parking spots
     */
    public List<ParkingSpot> getAvailableSpots() {
        return spotMap.values().stream()
                .filter(ParkingSpot::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Gets all available spots of a specific type.
     *
     * @param spotType the type of spot to filter by
     * @return a list of available spots of the given type
     */
    public List<ParkingSpot> getAvailableSpotsByType(ParkingSpotType spotType) {
        return spotMap.values().stream()
                .filter(spot -> spot.isAvailable() && spot.getSpotType() == spotType)
                .collect(Collectors.toList());
    }

    /**
     * Gets the count of available spots.
     *
     * @return the number of available spots
     */
    public int getAvailableSpotCount() {
        return spotMap.values().stream()
                .mapToInt(spot -> spot.isAvailable() ? 1 : 0)
                .sum();
    }

    /**
     * Gets all spots on this floor (including occupied ones).
     *
     * @return an unmodifiable collection of all spots
     */
    public Collection<ParkingSpot> getAllSpots() {
        return Collections.unmodifiableCollection(spotMap.values());
    }

    public String getFloorId() {
        return floorId;
    }

    public int getTotalSpots() {
        return totalSpots;
    }

    public int getOccupiedSpotCount() {
        return totalSpots - getAvailableSpotCount();
    }

    @Override
    public String toString() {
        return "ParkingFloor{" +
                "floorId='" + floorId + '\'' +
                ", totalSpots=" + totalSpots +
                ", occupiedSpots=" + getOccupiedSpotCount() +
                ", availableSpots=" + getAvailableSpotCount() +
                '}';
    }
}

