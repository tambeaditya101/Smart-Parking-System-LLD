package com.airtribe.parkinglot.utils;

import com.airtribe.parkinglot.domain.enums.ParkingSpotType;
import com.airtribe.parkinglot.domain.models.ParkingFloor;
import com.airtribe.parkinglot.domain.models.ParkingLot;

/**
 * ParkingDisplayBoard displays the real-time status of the parking lot.
 * Shows available spots by type for each floor and overall statistics.
 */
public class ParkingDisplayBoard {

    /**
     * Prints the current status of the entire parking lot.
     */
    public static void displayParkingLotStatus() {
        ParkingLot lot = ParkingLot.getInstance();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("                   PARKING LOT STATUS BOARD");
        System.out.println("=".repeat(70));

        // Overall statistics
        System.out.println("\n📊 Overall Statistics:");
        System.out.println("   Total Capacity: " + lot.getTotalCapacity() + " spots");
        System.out.println("   Occupied: " + lot.getTotalOccupiedSpots() + " spots");
        System.out.println("   Available: " + lot.getTotalAvailableSpots() + " spots");
        System.out.println("   Occupancy Rate: " + String.format("%.1f", lot.getOccupancyRate()) + "%");

        // Floor-by-floor breakdown
        System.out.println("\n📍 Floor Details:");
        System.out.println("-".repeat(70));

        for (ParkingFloor floor : lot.getFloors()) {
            displayFloorStatus(floor);
        }

        System.out.println("-".repeat(70));
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * Displays status for a single floor.
     *
     * @param floor the parking floor to display
     */
    private static void displayFloorStatus(ParkingFloor floor) {
        int smallAvailable = floor.getAvailableSpotsByType(ParkingSpotType.SMALL).size();
        int mediumAvailable = floor.getAvailableSpotsByType(ParkingSpotType.MEDIUM).size();
        int largeAvailable = floor.getAvailableSpotsByType(ParkingSpotType.LARGE).size();

        int totalSmall = (int) floor.getAllSpots().stream()
                .filter(s -> s.getSpotType() == ParkingSpotType.SMALL)
                .count();
        int totalMedium = (int) floor.getAllSpots().stream()
                .filter(s -> s.getSpotType() == ParkingSpotType.MEDIUM)
                .count();
        int totalLarge = (int) floor.getAllSpots().stream()
                .filter(s -> s.getSpotType() == ParkingSpotType.LARGE)
                .count();

        System.out.println("\n🏢 " + floor.getFloorId() + ":");
        System.out.println("   Total: " + floor.getTotalSpots() + " | Occupied: " + floor.getOccupiedSpotCount() +
                          " | Available: " + floor.getAvailableSpotCount());
        System.out.println("   ├─ SMALL:  " + smallAvailable + "/" + totalSmall + " available " +
                          getBarChart(smallAvailable, totalSmall));
        System.out.println("   ├─ MEDIUM: " + mediumAvailable + "/" + totalMedium + " available " +
                          getBarChart(mediumAvailable, totalMedium));
        System.out.println("   └─ LARGE:  " + largeAvailable + "/" + totalLarge + " available " +
                          getBarChart(largeAvailable, totalLarge));
    }

    /**
     * Generates a simple text bar chart showing availability.
     *
     * @param available number of available spots
     * @param total total spots of this type
     * @return a text-based bar chart
     */
    private static String getBarChart(int available, int total) {
        if (total == 0) return "[          ]";

        int filledLength = (available * 10) / total;
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < 10; i++) {
            if (i < filledLength) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");

        return bar.toString();
    }

    /**
     * Prints a quick summary status line (for updates during operations).
     */
    public static void displayQuickStatus() {
        ParkingLot lot = ParkingLot.getInstance();
        System.out.println("┌─ Lot Status: " + lot.getTotalOccupiedSpots() + "/" +
                          lot.getTotalCapacity() + " occupied (" +
                          String.format("%.1f", lot.getOccupancyRate()) + "%) ─┐");
    }
}

