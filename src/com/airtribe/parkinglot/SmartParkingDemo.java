package com.airtribe.parkinglot;

import com.airtribe.parkinglot.domain.enums.*;
import com.airtribe.parkinglot.domain.models.*;
import com.airtribe.parkinglot.domain.strategies.*;
import com.airtribe.parkinglot.exceptions.ParkingLotException;
import com.airtribe.parkinglot.exceptions.SpotNotFoundException;
import com.airtribe.parkinglot.services.ParkingLotManager;
import com.airtribe.parkinglot.utils.ParkingDisplayBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Main application demonstrating the Smart Parking System.
 * Simulates a 'Day in the Life' of the parking lot with concurrent vehicle entry/exit.
 */
public class SmartParkingDemo {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     SMART PARKING SYSTEM - LIVE DEMONSTRATION                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        try {
            // Initialize the parking lot
            initializeParkingLot();

            // Create the parking lot manager with initial strategies
            ParkingLotManager manager = new ParkingLotManager(
                    new NearestSpotStrategy(),
                    new HourlyFeeStrategy(),
                    new CreditCardPaymentProcessor()
            );

            // Display initial status
            System.out.println("\n[TIMESTAMP: 08:00 AM] Morning - Parking Lot Opens");
            ParkingDisplayBoard.displayParkingLotStatus();

            // Scenario 1: 5 vehicles arrive simultaneously
            System.out.println("\n[TIMESTAMP: 08:15 AM] 🚗 Morning Rush - 5 Vehicles Arriving Simultaneously");
            System.out.println("─".repeat(70));

            List<ParkingTicket> parkingTickets = simulateConcurrentEntry(manager, 5);
            int successfulParking = (int) parkingTickets.stream().filter(t -> t != null).count();

            System.out.println("\n✅ " + successfulParking + "/5 vehicles successfully parked");
            ParkingDisplayBoard.displayParkingLotStatus();

            // Scenario 2: Switch fee strategy
            System.out.println("\n[TIMESTAMP: 10:30 AM] 💰 Management Decision: Switching to Flat Rate Pricing");
            manager.setFeeCalculationStrategy(new FlatRateFeeStrategy());
            System.out.println("✓ Fee strategy switched to: " + manager.getFeeCalculationStrategy().getStrategyName());

            // Scenario 3: More vehicles arrive
            System.out.println("\n[TIMESTAMP: 11:00 AM] 🚗 More vehicles arriving...");
            List<ParkingTicket> moreTickets = simulateConcurrentEntry(manager, 3);
            System.out.println("✓ " + (int) moreTickets.stream().filter(t -> t != null).count() + " more vehicles parked");
            ParkingDisplayBoard.displayParkingLotStatus();

            // Scenario 4: Vehicles start exiting
            System.out.println("\n[TIMESTAMP: 12:00 PM] 🚗 Lunch Time - Some Vehicles Exiting");
            System.out.println("─".repeat(70));

            List<Receipt> receipts = new ArrayList<>();

            // Exit first vehicle
            if (parkingTickets.get(0) != null) {
                ParkingTicket exitingTicket = parkingTickets.get(0);
                System.out.println("\n🚗 Vehicle exiting: " + exitingTicket.getVehicle().getLicensePlate());
                Receipt receipt = manager.processExit(exitingTicket.getTicketId());

                if (receipt != null) {
                    receipts.add(receipt);
                    System.out.println("✅ Payment processed successfully!");
                    System.out.println("\n" + receipt.formatReceipt());
                } else {
                    System.out.println("❌ Payment processing failed");
                }
            }

            // Exit second vehicle with different fee strategy
            sleep(500); // Small delay to show the difference
            if (parkingTickets.get(1) != null) {
                ParkingTicket exitingTicket = parkingTickets.get(1);
                System.out.println("\n🚗 Vehicle exiting: " + exitingTicket.getVehicle().getLicensePlate());
                Receipt receipt = manager.processExit(exitingTicket.getTicketId());

                if (receipt != null) {
                    receipts.add(receipt);
                    System.out.println("✅ Payment processed successfully!");
                    System.out.println("\n" + receipt.formatReceipt());
                } else {
                    System.out.println("❌ Payment processing failed");
                }
            }

            ParkingDisplayBoard.displayParkingLotStatus();

            // Scenario 5: End of day report
            System.out.println("\n[TIMESTAMP: 06:00 PM] Evening Report");
            displayEndOfDayReport(manager, receipts);

            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║     DEMO COMPLETED SUCCESSFULLY                               ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes the parking lot with 3 floors, 10 spots each.
     */
    private static void initializeParkingLot() {
        ParkingLot.resetInstance();
        ParkingLot lot = ParkingLot.getInstance();

        System.out.println("\n[INITIALIZATION] Setting up parking lot...");

        for (int floorNum = 1; floorNum <= 3; floorNum++) {
            String floorId = "F" + floorNum;
            ParkingFloor floor = new ParkingFloor(floorId, 10);

            // Add spots: 3 SMALL, 4 MEDIUM, 3 LARGE per floor
            for (int i = 1; i <= 3; i++) {
                floor.addParkingSpot(new ParkingSpot(floorId + "-S" + i, ParkingSpotType.SMALL));
            }
            for (int i = 1; i <= 4; i++) {
                floor.addParkingSpot(new ParkingSpot(floorId + "-M" + i, ParkingSpotType.MEDIUM));
            }
            for (int i = 1; i <= 3; i++) {
                floor.addParkingSpot(new ParkingSpot(floorId + "-L" + i, ParkingSpotType.LARGE));
            }

            lot.addFloor(floor);
            System.out.println("   ✓ " + floorId + " created with 10 spots");
        }

        System.out.println("✅ Parking lot initialized: 3 floors, 30 total spots");
    }

    /**
     * Simulates concurrent vehicle entry using CompletableFuture.
     *
     * @param manager the parking lot manager
     * @param vehicleCount the number of vehicles to enter
     * @return list of parking tickets (null if parking failed)
     */
    private static List<ParkingTicket> simulateConcurrentEntry(ParkingLotManager manager, int vehicleCount) {
        List<CompletableFuture<ParkingTicket>> futures = new ArrayList<>();
        List<ParkingTicket> tickets = new ArrayList<>();

        VehicleType[] types = {VehicleType.MOTORCYCLE, VehicleType.CAR, VehicleType.VAN, VehicleType.BUS};
        String[] names = {"Alice", "Bob", "Charlie", "Diana", "Eve"};

        for (int i = 0; i < vehicleCount; i++) {
            final int index = i;
            CompletableFuture<ParkingTicket> future = CompletableFuture.supplyAsync(() -> {
                VehicleType type = types[index % types.length];
                String name = names[index % names.length];
                String licensePlate = type.name().substring(0, 3).toUpperCase() + "-" + (1000 + index);

                Vehicle vehicle = new Vehicle(licensePlate, type, name);
                ParkingTicket ticket = manager.issueTicket(vehicle);

                if (ticket != null) {
                    System.out.println("   ✓ " + name + " parked " + type.name() + " (" + licensePlate + ") in spot " +
                                     ticket.getParkingSpot().getSpotId());
                } else {
                    System.out.println("   ❌ " + name + " - No parking spot available for " + type.name());
                }

                return ticket;
            });

            futures.add(future);
        }

        // Wait for all parking operations to complete
        for (CompletableFuture<ParkingTicket> future : futures) {
            try {
                ParkingTicket ticket = future.get(5, TimeUnit.SECONDS);
                tickets.add(ticket);
            } catch (Exception e) {
                System.err.println("Error during concurrent entry: " + e.getMessage());
                tickets.add(null);
            }
        }

        return tickets;
    }

    /**
     * Displays end-of-day report.
     *
     * @param manager the parking lot manager
     * @param receipts list of payment receipts
     */
    private static void displayEndOfDayReport(ParkingLotManager manager, List<Receipt> receipts) {
        ParkingDisplayBoard.displayParkingLotStatus();

        System.out.println("\n📊 End of Day Summary:");
        System.out.println("─".repeat(70));
        System.out.println("   Total Transactions: " + manager.getTotalTicketCount());
        System.out.println("   Active Tickets: " + manager.getActiveTicketCount());
        System.out.println("   Completed Payments: " + receipts.size());

        double totalRevenue = receipts.stream().mapToDouble(Receipt::getAmount).sum();
        System.out.println("   Total Revenue: $" + String.format("%.2f", totalRevenue));

        if (!receipts.isEmpty()) {
            double avgFee = totalRevenue / receipts.size();
            System.out.println("   Average Fee: $" + String.format("%.2f", avgFee));
        }

        System.out.println("   Active Allocation Strategy: " + manager.getAllocationStrategy().getStrategyName());
        System.out.println("   Active Fee Strategy: " + manager.getFeeCalculationStrategy().getStrategyName());
        System.out.println("─".repeat(70));
    }

    /**
     * Helper method to pause execution.
     *
     * @param milliseconds the duration to sleep
     */
    private static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


