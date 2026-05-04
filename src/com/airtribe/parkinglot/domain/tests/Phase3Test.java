package com.airtribe.parkinglot.domain.tests;

import com.airtribe.parkinglot.domain.enums.*;
import com.airtribe.parkinglot.domain.models.*;
import com.airtribe.parkinglot.domain.strategies.*;
import com.airtribe.parkinglot.services.ParkingLotManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test class to verify Phase 3: Orchestration & Allocation Layer.
 */
public class Phase3Test {

    public static void main(String[] args) {
        System.out.println("=== Phase 3: Orchestration & Allocation Layer Test ===\n");

        testParkingLotSingleton();
        testNearestSpotStrategy();
        testBestFitSpotStrategy();
        testTicketIssuanceAndProcessing();
        testConcurrentEntryAndExit();
        testRuntimeStrategySwapping();
        testIntegrationFullScenario();

        System.out.println("\n=== All Phase 3 tests passed! ===");
    }

    private static void testParkingLotSingleton() {
        System.out.println("Test 1: ParkingLot Singleton Pattern");

        // Reset singleton for clean test
        ParkingLot.resetInstance();

        ParkingLot lot1 = ParkingLot.getInstance();
        ParkingLot lot2 = ParkingLot.getInstance();

        assert lot1 == lot2 : "Singleton should return the same instance";
        assert "PARKING-LOT-001".equals(lot1.getLotId()) : "Lot ID should match";

        // Add floors
        ParkingFloor floor1 = new ParkingFloor("F1", 10);
        ParkingFloor floor2 = new ParkingFloor("F2", 10);

        assert lot1.addFloor(floor1) : "Floor 1 should be added";
        assert lot1.addFloor(floor2) : "Floor 2 should be added";
        assert lot1.getFloorCount() == 2 : "Should have 2 floors";

        // Add spots to floors
        for (int i = 1; i <= 5; i++) {
            floor1.addParkingSpot(new ParkingSpot("F1-S" + i, ParkingSpotType.SMALL));
        }
        for (int i = 1; i <= 5; i++) {
            floor1.addParkingSpot(new ParkingSpot("F1-M" + i, ParkingSpotType.MEDIUM));
        }

        assert lot1.getTotalCapacity() == 10 : "Total capacity should be 10 for floor 1";
        assert lot1.getTotalAvailableSpots() == 10 : "All spots should be available";
        assert lot1.getTotalOccupiedSpots() == 0 : "No spots should be occupied";
        assert lot1.getOccupancyRate() == 0.0 : "Occupancy rate should be 0%";

        System.out.println("  ✓ ParkingLot singleton tests passed\n");
    }

    private static void testNearestSpotStrategy() {
        System.out.println("Test 2: Nearest Spot Strategy");

        ParkingLot.resetInstance();
        ParkingLot lot = ParkingLot.getInstance();

        // Setup: Create 2 floors with mixed spot types
        ParkingFloor floor1 = new ParkingFloor("F1", 5);
        floor1.addParkingSpot(new ParkingSpot("F1-S1", ParkingSpotType.SMALL));
        floor1.addParkingSpot(new ParkingSpot("F1-M1", ParkingSpotType.MEDIUM));
        floor1.addParkingSpot(new ParkingSpot("F1-L1", ParkingSpotType.LARGE));

        ParkingFloor floor2 = new ParkingFloor("F2", 5);
        floor2.addParkingSpot(new ParkingSpot("F2-M1", ParkingSpotType.MEDIUM));
        floor2.addParkingSpot(new ParkingSpot("F2-L1", ParkingSpotType.LARGE));

        lot.addFloor(floor1);
        lot.addFloor(floor2);

        // Test strategy
        SpotAllocationStrategy strategy = new NearestSpotStrategy();

        // For motorcycle, should find SMALL spot first (F1-S1)
        ParkingSpot spot1 = strategy.findSpot(VehicleType.MOTORCYCLE);
        assert spot1 != null : "Should find a spot for motorcycle";
        assert spot1.getSpotId().equals("F1-S1") : "Should get F1-S1 for motorcycle (nearest)";

        // Park the motorcycle
        Vehicle moto = new Vehicle("MOTO001", VehicleType.MOTORCYCLE, "Owner");
        assert spot1.parkVehicle(moto) : "Motorcycle should park";

        // Next motorcycle should get F1-M1 or F1-L1 (since F1-S1 is occupied)
        ParkingSpot spot2 = strategy.findSpot(VehicleType.MOTORCYCLE);
        assert spot2 != null : "Should find another spot for second motorcycle";
        assert spot2.getSpotId().equals("F1-M1") : "Should get F1-M1 (next available on F1)";

        // For CAR, should find MEDIUM spot on F1
        ParkingSpot spot3 = strategy.findSpot(VehicleType.CAR);
        assert spot3 != null : "Should find a spot for car";
        assert spot3.getSpotId().equals("F1-L1") : "Should get F1-L1 (nearest MEDIUM or larger)";

        System.out.println("  ✓ Nearest spot strategy tests passed\n");
    }

    private static void testBestFitSpotStrategy() {
        System.out.println("Test 3: Best Fit Spot Strategy");

        ParkingLot.resetInstance();
        ParkingLot lot = ParkingLot.getInstance();

        // Setup: Create 1 floor with all spot types
        ParkingFloor floor = new ParkingFloor("F1", 10);
        floor.addParkingSpot(new ParkingSpot("F1-S1", ParkingSpotType.SMALL));
        floor.addParkingSpot(new ParkingSpot("F1-S2", ParkingSpotType.SMALL));
        floor.addParkingSpot(new ParkingSpot("F1-M1", ParkingSpotType.MEDIUM));
        floor.addParkingSpot(new ParkingSpot("F1-M2", ParkingSpotType.MEDIUM));
        floor.addParkingSpot(new ParkingSpot("F1-L1", ParkingSpotType.LARGE));

        lot.addFloor(floor);

        SpotAllocationStrategy strategy = new BestFitSpotStrategy();

        // For motorcycle, should get SMALL spot first (best fit)
        ParkingSpot spot1 = strategy.findSpot(VehicleType.MOTORCYCLE);
        assert spot1 != null : "Should find a spot for motorcycle";
        assert spot1.getSpotType() == ParkingSpotType.SMALL : "Should prefer SMALL for motorcycle";
        assert spot1.getSpotId().equals("F1-S1") : "Should get F1-S1";

        // Park motorcycle
        Vehicle moto = new Vehicle("MOTO001", VehicleType.MOTORCYCLE, "Owner");
        spot1.parkVehicle(moto);

        // Second motorcycle should get next SMALL spot
        ParkingSpot spot2 = strategy.findSpot(VehicleType.MOTORCYCLE);
        assert spot2 != null : "Should find another spot";
        assert spot2.getSpotType() == ParkingSpotType.SMALL : "Should prefer SMALL";
        assert spot2.getSpotId().equals("F1-S2") : "Should get F1-S2";

        // Park second motorcycle
        Vehicle moto2 = new Vehicle("MOTO002", VehicleType.MOTORCYCLE, "Owner");
        spot2.parkVehicle(moto2);

        // Third motorcycle should get MEDIUM spot (no SMALL left)
        ParkingSpot spot3 = strategy.findSpot(VehicleType.MOTORCYCLE);
        assert spot3 != null : "Should find another spot";
        assert spot3.getSpotType() == ParkingSpotType.MEDIUM : "Should use MEDIUM when SMALL full";

        // For CAR, should get MEDIUM spot (best fit)
        ParkingSpot spot4 = strategy.findSpot(VehicleType.CAR);
        assert spot4 != null : "Should find a spot for car";
        assert spot4.getSpotType() == ParkingSpotType.MEDIUM : "Should use MEDIUM for car (best fit)";

        System.out.println("  ✓ Best fit spot strategy tests passed\n");
    }

    private static void testTicketIssuanceAndProcessing() {
        System.out.println("Test 4: Ticket Issuance and Processing");

        ParkingLot.resetInstance();
        setupParkingLot();

        ParkingLotManager manager = new ParkingLotManager(
                new NearestSpotStrategy(),
                new HourlyFeeStrategy(),
                new CreditCardPaymentProcessor()
        );

        // Test ticket issuance
        Vehicle car = new Vehicle("CAR001", VehicleType.CAR, "John Doe");
        ParkingTicket ticket = manager.issueTicket(car);

        assert ticket != null : "Ticket should be issued";
        assert ticket.getStatus() == TicketStatus.ACTIVE : "Ticket should be active";
        assert ticket.getVehicle().equals(car) : "Ticket vehicle should match";
        assert ticket.getAmountDue() == 0.0 : "Initial amount due should be 0";
        assert manager.getActiveTicketCount() == 1 : "Should have 1 active ticket";

        // Test exit processing
        Receipt receipt = manager.processExit(ticket.getTicketId());
        assert receipt != null : "Receipt should be generated";
        assert receipt.getTicketId().equals(ticket.getTicketId()) : "Receipt ticket ID should match";
        assert manager.getActiveTicketCount() == 0 : "Should have 0 active tickets after exit";
        assert ticket.getStatus() == TicketStatus.PAID : "Ticket should be marked as paid";

        System.out.println("  ✓ Ticket issuance and processing tests passed\n");
    }

    private static void testConcurrentEntryAndExit() throws AssertionError {
        System.out.println("Test 5: Concurrent Entry/Exit (Thread-safe)");

        ParkingLot.resetInstance();
        setupParkingLot();

        ParkingLotManager manager = new ParkingLotManager(
                new NearestSpotStrategy(),
                new HourlyFeeStrategy(),
                new CreditCardPaymentProcessor()
        );

        int vehicleCount = 20;
        CountDownLatch entryLatch = new CountDownLatch(vehicleCount);
        CountDownLatch exitLatch = new CountDownLatch(vehicleCount);
        AtomicInteger successfulEntries = new AtomicInteger(0);
        AtomicInteger successfulExits = new AtomicInteger(0);
        ParkingTicket[] tickets = new ParkingTicket[vehicleCount];

        // Concurrent entry
        for (int i = 0; i < vehicleCount; i++) {
            final int index = i;
            new Thread(() -> {
                VehicleType[] types = {VehicleType.MOTORCYCLE, VehicleType.CAR, VehicleType.VAN};
                Vehicle vehicle = new Vehicle(
                        "CAR" + index,
                        types[index % types.length],
                        "Owner " + index
                );
                ParkingTicket ticket = manager.issueTicket(vehicle);
                if (ticket != null) {
                    tickets[index] = ticket;
                    successfulEntries.incrementAndGet();
                }
                entryLatch.countDown();
            }).start();
        }

        try {
            entryLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int parked = successfulEntries.get();
        System.out.println("  Vehicles parked: " + parked + "/" + vehicleCount);
        assert parked > 0 : "Some vehicles should find parking";
        assert parked <= manager.getParkingLot().getTotalCapacity() : "Parked count should not exceed capacity";

        // Concurrent exit
        for (int i = 0; i < vehicleCount; i++) {
            final int index = i;
            new Thread(() -> {
                if (tickets[index] != null) {
                    Receipt receipt = manager.processExit(tickets[index].getTicketId());
                    if (receipt != null) {
                        successfulExits.incrementAndGet();
                    }
                }
                exitLatch.countDown();
            }).start();
        }

        try {
            exitLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("  Vehicles exited successfully: " + successfulExits.get() + "/" + parked);
        assert successfulExits.get() == parked : "All parked vehicles should exit successfully";
        assert manager.getParkingLot().getTotalOccupiedSpots() == 0 : "Lot should be empty after all exits";

        System.out.println("  ✓ Concurrent entry/exit tests passed\n");
    }

    private static void testRuntimeStrategySwapping() {
        System.out.println("Test 6: Runtime Strategy Swapping");

        ParkingLot.resetInstance();
        setupParkingLot();

        ParkingLotManager manager = new ParkingLotManager(
                new NearestSpotStrategy(),
                new HourlyFeeStrategy(),
                new CreditCardPaymentProcessor()
        );

        // Test initial strategy
        assert manager.getAllocationStrategy().getStrategyName().contains("Nearest") : "Should start with Nearest strategy";

        // Swap to BestFit
        manager.setAllocationStrategy(new BestFitSpotStrategy());
        assert manager.getAllocationStrategy().getStrategyName().contains("Best Fit") : "Should swap to Best Fit strategy";

        // Verify new strategy is used
        Vehicle moto = new Vehicle("MOTO001", VehicleType.MOTORCYCLE, "Owner");
        ParkingTicket ticket = manager.issueTicket(moto);
        assert ticket.getParkingSpot().getSpotType() == ParkingSpotType.SMALL : "BestFit should allocate SMALL for motorcycle";

        // Swap fee strategy
        assert manager.getFeeCalculationStrategy() instanceof HourlyFeeStrategy : "Should start with Hourly fee";
        manager.setFeeCalculationStrategy(new FlatRateFeeStrategy());
        assert manager.getFeeCalculationStrategy() instanceof FlatRateFeeStrategy : "Should swap to Flat rate fee";

        // Exit and verify new fee strategy
        Receipt receipt = manager.processExit(ticket.getTicketId());
        assert receipt != null : "Should process exit with new fee strategy";

        System.out.println("  ✓ Runtime strategy swapping tests passed\n");
    }

    private static void testIntegrationFullScenario() {
        System.out.println("Test 7: Full Integration Scenario");

        ParkingLot.resetInstance();
        setupParkingLot();

        // Setup manager with multiple strategies available for switching
        ParkingLotManager manager = new ParkingLotManager(
                new NearestSpotStrategy(),
                new HourlyFeeStrategy(),
                new CreditCardPaymentProcessor()
        );

        System.out.println("  Parking Lot Status: " + manager.getParkingLot());

        // Scenario 1: Car parks with Hourly pricing
        Vehicle car1 = new Vehicle("CAR-INTG-001", VehicleType.CAR, "Alice");
        ParkingTicket ticket1 = manager.issueTicket(car1);
        assert ticket1 != null : "Car should park";
        System.out.println("  ✓ Car 1 parked (Hourly pricing)");

        // Scenario 2: Motorcycle parks
        Vehicle moto1 = new Vehicle("MOTO-INTG-001", VehicleType.MOTORCYCLE, "Bob");
        ParkingTicket ticket2 = manager.issueTicket(moto1);
        assert ticket2 != null : "Motorcycle should park";
        System.out.println("  ✓ Motorcycle parked");

        // Scenario 3: Switch to flat rate and issue new ticket
        manager.setFeeCalculationStrategy(new FlatRateFeeStrategy());
        Vehicle car2 = new Vehicle("CAR-INTG-002", VehicleType.CAR, "Charlie");
        ParkingTicket ticket3 = manager.issueTicket(car2);
        assert ticket3 != null : "Car should park";
        System.out.println("  ✓ Car 2 parked (Flat rate pricing)");

        // Scenario 4: Process exits
        Receipt receipt1 = manager.processExit(ticket1.getTicketId());
        assert receipt1 != null : "Car 1 payment should succeed";
        System.out.println("  ✓ Car 1 exited (Hourly: " + receipt1.getAmount() + ")");

        Receipt receipt2 = manager.processExit(ticket2.getTicketId());
        assert receipt2 != null : "Motorcycle payment should succeed";
        System.out.println("  ✓ Motorcycle exited (Hourly: " + receipt2.getAmount() + ")");

        Receipt receipt3 = manager.processExit(ticket3.getTicketId());
        assert receipt3 != null : "Car 2 payment should succeed";
        System.out.println("  ✓ Car 2 exited (Flat rate: " + receipt3.getAmount() + ")");

        // Final status
        System.out.println("  Final Parking Lot Status: " + manager.getParkingLot());
        assert manager.getParkingLot().getTotalOccupiedSpots() == 0 : "Lot should be empty";

        System.out.println("  ✓ Full integration scenario tests passed\n");
    }

    private static void setupParkingLot() {
        ParkingLot lot = ParkingLot.getInstance();

        // Create 2 floors with mixed spot types
        ParkingFloor floor1 = new ParkingFloor("F1", 20);
        for (int i = 1; i <= 5; i++) {
            floor1.addParkingSpot(new ParkingSpot("F1-S" + i, ParkingSpotType.SMALL));
        }
        for (int i = 1; i <= 8; i++) {
            floor1.addParkingSpot(new ParkingSpot("F1-M" + i, ParkingSpotType.MEDIUM));
        }
        for (int i = 1; i <= 7; i++) {
            floor1.addParkingSpot(new ParkingSpot("F1-L" + i, ParkingSpotType.LARGE));
        }

        ParkingFloor floor2 = new ParkingFloor("F2", 20);
        for (int i = 1; i <= 5; i++) {
            floor2.addParkingSpot(new ParkingSpot("F2-S" + i, ParkingSpotType.SMALL));
        }
        for (int i = 1; i <= 8; i++) {
            floor2.addParkingSpot(new ParkingSpot("F2-M" + i, ParkingSpotType.MEDIUM));
        }
        for (int i = 1; i <= 7; i++) {
            floor2.addParkingSpot(new ParkingSpot("F2-L" + i, ParkingSpotType.LARGE));
        }

        lot.addFloor(floor1);
        lot.addFloor(floor2);
    }
}

