package com.airtribe.parkinglot.domain.tests;

import com.airtribe.parkinglot.domain.enums.ParkingSpotType;
import com.airtribe.parkinglot.domain.enums.VehicleType;
import com.airtribe.parkinglot.domain.models.ParkingFloor;
import com.airtribe.parkinglot.domain.models.ParkingSpot;
import com.airtribe.parkinglot.domain.models.Vehicle;

/**
 * Test class to verify Phase 1 domain models and allocation rules.
 */
public class Phase1Test {

    public static void main(String[] args) {
        System.out.println("=== Phase 1: Core Domain & Enums Test ===\n");

        testAllocationRules();
        testVehicleClass();
        testParkingSpot();
        testParkingFloor();
        testThreadSafety();

        System.out.println("\n=== All tests passed! ===");
    }

    private static void testAllocationRules() {
        System.out.println("Test 1: Allocation Rules");

        // SMALL spot tests
        System.out.println("  SMALL spot can fit MOTORCYCLE: " + ParkingSpotType.SMALL.canFit(VehicleType.MOTORCYCLE));
        assert ParkingSpotType.SMALL.canFit(VehicleType.MOTORCYCLE) : "SMALL should fit MOTORCYCLE";
        assert !ParkingSpotType.SMALL.canFit(VehicleType.CAR) : "SMALL should NOT fit CAR";
        assert !ParkingSpotType.SMALL.canFit(VehicleType.VAN) : "SMALL should NOT fit VAN";
        assert !ParkingSpotType.SMALL.canFit(VehicleType.BUS) : "SMALL should NOT fit BUS";
        assert !ParkingSpotType.SMALL.canFit(null) : "SMALL should NOT fit null";

        // MEDIUM spot tests
        System.out.println("  MEDIUM spot can fit MOTORCYCLE, CAR, VAN: " +
                ParkingSpotType.MEDIUM.canFit(VehicleType.MOTORCYCLE) + ", " +
                ParkingSpotType.MEDIUM.canFit(VehicleType.CAR) + ", " +
                ParkingSpotType.MEDIUM.canFit(VehicleType.VAN));
        assert ParkingSpotType.MEDIUM.canFit(VehicleType.MOTORCYCLE) : "MEDIUM should fit MOTORCYCLE";
        assert ParkingSpotType.MEDIUM.canFit(VehicleType.CAR) : "MEDIUM should fit CAR";
        assert ParkingSpotType.MEDIUM.canFit(VehicleType.VAN) : "MEDIUM should fit VAN";
        assert !ParkingSpotType.MEDIUM.canFit(VehicleType.BUS) : "MEDIUM should NOT fit BUS";
        assert !ParkingSpotType.MEDIUM.canFit(null) : "MEDIUM should NOT fit null";

        // LARGE spot tests
        System.out.println("  LARGE spot can fit all types: " +
                ParkingSpotType.LARGE.canFit(VehicleType.MOTORCYCLE) + ", " +
                ParkingSpotType.LARGE.canFit(VehicleType.CAR) + ", " +
                ParkingSpotType.LARGE.canFit(VehicleType.VAN) + ", " +
                ParkingSpotType.LARGE.canFit(VehicleType.BUS));
        assert ParkingSpotType.LARGE.canFit(VehicleType.MOTORCYCLE) : "LARGE should fit MOTORCYCLE";
        assert ParkingSpotType.LARGE.canFit(VehicleType.CAR) : "LARGE should fit CAR";
        assert ParkingSpotType.LARGE.canFit(VehicleType.VAN) : "LARGE should fit VAN";
        assert ParkingSpotType.LARGE.canFit(VehicleType.BUS) : "LARGE should fit BUS";
        assert !ParkingSpotType.LARGE.canFit(null) : "LARGE should NOT fit null";

        System.out.println("  ✓ All allocation rules passed\n");
    }

    private static void testVehicleClass() {
        System.out.println("Test 2: Vehicle Class");

        Vehicle v1 = new Vehicle("ABC123", VehicleType.CAR, "John Doe");
        Vehicle v2 = new Vehicle("ABC123", VehicleType.MOTORCYCLE, "Jane Doe");
        Vehicle v3 = new Vehicle("XYZ789", VehicleType.CAR, null);

        assert "ABC123".equals(v1.getLicensePlate()) : "License plate should match";
        assert v1.getVehicleType() == VehicleType.CAR : "Vehicle type should be CAR";
        assert "John Doe".equals(v1.getOwnerName()) : "Owner name should match";
        assert "Unknown".equals(v3.getOwnerName()) : "Unknown owner name should default to 'Unknown'";
        assert v1.equals(v2) : "Vehicles with same license plate should be equal";
        System.out.println("  ✓ Vehicle class tests passed\n");
    }

    private static void testParkingSpot() {
        System.out.println("Test 3: ParkingSpot Class (Thread-safe availability)");

        ParkingSpot spot = new ParkingSpot("A1", ParkingSpotType.MEDIUM);
        assert spot.isAvailable() : "Spot should be available initially";
        assert spot.getParkedVehicle() == null : "No vehicle should be parked initially";

        // Test parking a compatible vehicle
        Vehicle car = new Vehicle("CAR001", VehicleType.CAR, "Owner 1");
        assert spot.parkVehicle(car) : "Car should park in MEDIUM spot";
        assert !spot.isAvailable() : "Spot should not be available after parking";
        assert car.equals(spot.getParkedVehicle()) : "Parked vehicle should match";

        // Try parking another vehicle (should fail)
        Vehicle van = new Vehicle("VAN001", VehicleType.VAN, "Owner 2");
        assert !spot.parkVehicle(van) : "Second vehicle should not park (spot occupied)";

        // Test unparking
        Vehicle unparked = spot.unparkVehicle();
        assert car.equals(unparked) : "Unparked vehicle should match the original";
        assert spot.isAvailable() : "Spot should be available after unparking";
        assert spot.getParkedVehicle() == null : "No vehicle should be parked after unparking";

        // Test parking incompatible vehicle (BUS in MEDIUM spot)
        ParkingSpot mediumSpot = new ParkingSpot("A2", ParkingSpotType.MEDIUM);
        Vehicle bus = new Vehicle("BUS001", VehicleType.BUS, "Owner 3");
        assert !mediumSpot.parkVehicle(bus) : "Bus should not park in MEDIUM spot";
        assert mediumSpot.isAvailable() : "MEDIUM spot should remain available";

        System.out.println("  ✓ ParkingSpot tests passed\n");
    }

    private static void testParkingFloor() {
        System.out.println("Test 4: ParkingFloor Class");

        ParkingFloor floor = new ParkingFloor("F1", 5);
        assert floor.getFloorId().equals("F1") : "Floor ID should match";
        assert floor.getTotalSpots() == 5 : "Total spots should be 5";
        assert floor.getAvailableSpotCount() == 0 : "No spots yet";

        // Add parking spots
        ParkingSpot spot1 = new ParkingSpot("F1-A1", ParkingSpotType.SMALL);
        ParkingSpot spot2 = new ParkingSpot("F1-A2", ParkingSpotType.MEDIUM);
        ParkingSpot spot3 = new ParkingSpot("F1-A3", ParkingSpotType.LARGE);
        ParkingSpot spot4 = new ParkingSpot("F1-A4", ParkingSpotType.MEDIUM);
        ParkingSpot spot5 = new ParkingSpot("F1-A5", ParkingSpotType.LARGE);

        assert floor.addParkingSpot(spot1) : "Spot 1 should be added";
        assert floor.addParkingSpot(spot2) : "Spot 2 should be added";
        assert floor.addParkingSpot(spot3) : "Spot 3 should be added";
        assert floor.addParkingSpot(spot4) : "Spot 4 should be added";
        assert floor.addParkingSpot(spot5) : "Spot 5 should be added";

        assert floor.getAvailableSpotCount() == 5 : "All 5 spots should be available";

        // Park a vehicle
        Vehicle car = new Vehicle("CAR001", VehicleType.CAR, "Owner");
        assert spot2.parkVehicle(car) : "Car should park in spot2";
        assert floor.getAvailableSpotCount() == 4 : "4 spots should be available after parking";
        assert floor.getOccupiedSpotCount() == 1 : "1 spot should be occupied";

        // Get available spots by type
        assert floor.getAvailableSpotsByType(ParkingSpotType.SMALL).size() == 1 : "1 SMALL spot available";
        assert floor.getAvailableSpotsByType(ParkingSpotType.MEDIUM).size() == 1 : "1 MEDIUM spot available (one is occupied)";
        assert floor.getAvailableSpotsByType(ParkingSpotType.LARGE).size() == 2 : "2 LARGE spots available";

        System.out.println("  ✓ ParkingFloor tests passed\n");
    }

    private static void testThreadSafety() {
        System.out.println("Test 5: Thread-safety (concurrent parking/unparking)");

        ParkingSpot spot = new ParkingSpot("CONCURRENT-1", ParkingSpotType.LARGE);
        Vehicle[] vehicles = new Vehicle[10];
        for (int i = 0; i < 10; i++) {
            vehicles[i] = new Vehicle("V" + i, VehicleType.CAR, "Owner " + i);
        }

        // Try to park multiple vehicles concurrently (only one should succeed)
        Thread[] threads = new Thread[10];
        int[] parkResults = new int[10];

        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                parkResults[index] = spot.parkVehicle(vehicles[index]) ? 1 : 0;
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Only one vehicle should have parked successfully
        int successCount = 0;
        for (int result : parkResults) {
            successCount += result;
        }
        assert successCount == 1 : "Only one vehicle should park successfully (got " + successCount + ")";
        assert !spot.isAvailable() : "Spot should not be available";

        System.out.println("  ✓ Thread-safety tests passed\n");
    }
}

