package com.airtribe.parkinglot.domain.enums;

/**
 * Enum representing different types of vehicles that can park in a parking lot.
 */
public enum VehicleType {
    MOTORCYCLE("Two-wheeled vehicle"),
    CAR("Standard four-wheeled vehicle"),
    VAN("Multi-passenger vehicle"),
    BUS("Large public transport vehicle");

    private final String description;

    VehicleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

