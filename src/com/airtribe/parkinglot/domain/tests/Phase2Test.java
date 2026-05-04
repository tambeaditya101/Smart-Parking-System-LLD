package com.airtribe.parkinglot.domain.tests;

import com.airtribe.parkinglot.domain.enums.*;
import com.airtribe.parkinglot.domain.models.*;
import com.airtribe.parkinglot.domain.strategies.*;

import java.time.LocalDateTime;

/**
 * Test class to verify Phase 2: Tickets, Payment, and Fees.
 */
public class Phase2Test {

    public static void main(String[] args) {
        System.out.println("=== Phase 2: Tickets, Payment, and Fees Test ===\n");

        testTicketCreationAndCompletion();
        testHourlyFeeStrategy();
        testFlatRateFeeStrategy();
        testReceiptGeneration();
        testPaymentProcessing();
        testIntegrationScenario();
        testStrategySubstitution();

        System.out.println("\n=== All Phase 2 tests passed! ===");
    }

    private static void testTicketCreationAndCompletion() {
        System.out.println("Test 1: Ticket Creation and Completion");

        Vehicle car = new Vehicle("CAR001", VehicleType.CAR, "John Doe");
        ParkingSpot spot = new ParkingSpot("A1", ParkingSpotType.MEDIUM);
        LocalDateTime entryTime = LocalDateTime.of(2024, 5, 1, 10, 0);

        ParkingTicket ticket = new ParkingTicket("TICKET001", car, spot, entryTime);

        assert "TICKET001".equals(ticket.getTicketId()) : "Ticket ID should match";
        assert ticket.getStatus() == TicketStatus.ACTIVE : "Initial status should be ACTIVE";
        assert ticket.getAmountDue() == 0.0 : "Initial amount should be 0";

        // Complete ticket with fee
        LocalDateTime exitTime = LocalDateTime.of(2024, 5, 1, 12, 30); // 2.5 hours
        ticket.completeTicket(exitTime, 25.0);

        assert ticket.getStatus() == TicketStatus.COMPLETED : "Status should be COMPLETED";
        assert ticket.getAmountDue() == 25.0 : "Amount due should be 25.0";
        assert ticket.getDurationInMinutes() == 150 : "Duration should be 150 minutes";

        System.out.println("  ✓ Ticket creation and completion tests passed\n");
    }

    private static void testHourlyFeeStrategy() {
        System.out.println("Test 2: Hourly Fee Strategy");

        FeeCalculationStrategy strategy = new HourlyFeeStrategy();

        // Test MOTORCYCLE: $5/hr
        double motoFee30min = strategy.calculateFee(30, VehicleType.MOTORCYCLE);
        assert motoFee30min == 5.0 : "30 min motorcycle should be $5 (min 1 hour)";

        double motoFee60min = strategy.calculateFee(60, VehicleType.MOTORCYCLE);
        assert motoFee60min == 5.0 : "60 min motorcycle should be $5";

        double motoFee90min = strategy.calculateFee(90, VehicleType.MOTORCYCLE);
        assert motoFee90min == 10.0 : "90 min motorcycle should be $10 (2 hours)";

        // Test CAR: $10/hr
        double carFee60min = strategy.calculateFee(60, VehicleType.CAR);
        assert carFee60min == 10.0 : "60 min car should be $10";

        double carFee120min = strategy.calculateFee(120, VehicleType.CAR);
        assert carFee120min == 20.0 : "120 min car should be $20";

        // Test VAN: $15/hr
        double vanFee60min = strategy.calculateFee(60, VehicleType.VAN);
        assert vanFee60min == 15.0 : "60 min van should be $15";

        // Test BUS: $20/hr
        double busFee60min = strategy.calculateFee(60, VehicleType.BUS);
        assert busFee60min == 20.0 : "60 min bus should be $20";

        double busFee180min = strategy.calculateFee(180, VehicleType.BUS);
        assert busFee180min == 60.0 : "180 min bus should be $60 (3 hours)";

        System.out.println("  ✓ Hourly fee strategy tests passed\n");
    }

    private static void testFlatRateFeeStrategy() {
        System.out.println("Test 3: Flat Rate Fee Strategy");

        FeeCalculationStrategy strategy = new FlatRateFeeStrategy();

        // Should always return $20 regardless of vehicle type or duration
        double fee30min = strategy.calculateFee(30, VehicleType.CAR);
        assert fee30min == 20.0 : "Flat rate for 30 min should be $20";

        double fee60min = strategy.calculateFee(60, VehicleType.MOTORCYCLE);
        assert fee60min == 20.0 : "Flat rate for 60 min motorcycle should be $20";

        double fee300min = strategy.calculateFee(300, VehicleType.BUS);
        assert fee300min == 20.0 : "Flat rate for 300 min bus should be $20";

        System.out.println("  ✓ Flat rate fee strategy tests passed\n");
    }

    private static void testReceiptGeneration() {
        System.out.println("Test 4: Receipt Generation");

        Receipt receipt = new Receipt(
                "RECEIPT001",
                "TICKET001",
                25.0,
                LocalDateTime.of(2024, 5, 1, 13, 0),
                PaymentMethod.CREDIT_CARD
        );

        assert "RECEIPT001".equals(receipt.getReceiptId()) : "Receipt ID should match";
        assert "TICKET001".equals(receipt.getTicketId()) : "Ticket ID should match";
        assert receipt.getAmount() == 25.0 : "Amount should be 25.0";
        assert receipt.getPaymentMethod() == PaymentMethod.CREDIT_CARD : "Payment method should be CREDIT_CARD";
        assert receipt.getPaymentStatus() == PaymentStatus.COMPLETED : "Payment status should be COMPLETED";

        // Test formatted receipt
        String formatted = receipt.formatReceipt();
        assert formatted.contains("RECEIPT001") : "Formatted receipt should contain receipt ID";
        assert formatted.contains("TICKET001") : "Formatted receipt should contain ticket ID";
        assert formatted.contains("25.00") : "Formatted receipt should contain amount";

        System.out.println("  ✓ Receipt generation tests passed\n");
    }

    private static void testPaymentProcessing() {
        System.out.println("Test 5: Credit Card Payment Processing");

        PaymentProcessor processor = new CreditCardPaymentProcessor();

        // Process multiple payments to verify success rate
        int successCount = 0;
        for (int i = 0; i < 10; i++) {
            Receipt receipt = processor.processPayment("TICKET00" + i, 25.0);
            if (receipt != null) {
                successCount++;
                assert receipt.getTicketId().equals("TICKET00" + i) : "Ticket ID should match";
                assert receipt.getAmount() == 25.0 : "Amount should match";
                assert receipt.getPaymentMethod() == PaymentMethod.CREDIT_CARD : "Method should be credit card";
            }
        }

        // Should have mostly successful payments (90%+ success rate expected)
        assert successCount >= 7 : "At least 70% of payments should succeed (got " + successCount + "/10)";
        System.out.println("  Payment success rate: " + (successCount * 10) + "% (" + successCount + "/10)");

        System.out.println("  ✓ Credit card payment processing tests passed\n");
    }

    private static void testIntegrationScenario() {
        System.out.println("Test 6: Integration Scenario (Ticket → Fee Calc → Payment)");

        // Setup
        Vehicle car = new Vehicle("CAR-INT-001", VehicleType.CAR, "Alice");
        ParkingSpot spot = new ParkingSpot("B5", ParkingSpotType.MEDIUM);
        FeeCalculationStrategy feeStrategy = new HourlyFeeStrategy();
        PaymentProcessor paymentProcessor = new CreditCardPaymentProcessor();

        LocalDateTime entryTime = LocalDateTime.of(2024, 5, 2, 9, 0);
        LocalDateTime exitTime = LocalDateTime.of(2024, 5, 2, 11, 0); // 2 hours

        // 1. Park vehicle
        assert spot.parkVehicle(car) : "Vehicle should park";

        // 2. Create ticket
        String ticketId = "TICKET-INT-" + System.nanoTime();
        ParkingTicket ticket = new ParkingTicket(ticketId, car, spot, entryTime);

        // 3. Exit and calculate fee
        long durationInMinutes = 120;
        double calculatedFee = feeStrategy.calculateFee(durationInMinutes, car.getVehicleType());
        assert calculatedFee == 20.0 : "Fee for 2-hour car should be $20";

        // 4. Complete ticket
        ticket.completeTicket(exitTime, calculatedFee);
        assert ticket.getStatus() == TicketStatus.COMPLETED : "Ticket should be completed";
        assert ticket.getAmountDue() == 20.0 : "Amount due should be $20";

        // 5. Process payment
        Receipt receipt = paymentProcessor.processPayment(ticketId, ticket.getAmountDue());
        assert receipt != null : "Payment should succeed";

        // 6. Mark ticket as paid
        ticket.markAsPaid();
        assert ticket.getStatus() == TicketStatus.PAID : "Ticket should be marked as paid";

        // 7. Unpark vehicle
        Vehicle unparked = spot.unparkVehicle();
        assert car.equals(unparked) : "Unparked vehicle should match";
        assert spot.isAvailable() : "Spot should be available after unparking";

        System.out.println("  ✓ Integration scenario tests passed\n");
    }

    private static void testStrategySubstitution() {
        System.out.println("Test 7: Runtime Strategy Substitution");

        Vehicle bus = new Vehicle("BUS001", VehicleType.BUS, "Transit Co");
        long duration = 60; // 1 hour

        // Strategy 1: Hourly
        FeeCalculationStrategy hourlyStrategy = new HourlyFeeStrategy();
        double hourlyFee = hourlyStrategy.calculateFee(duration, bus.getVehicleType());
        assert hourlyFee == 20.0 : "Hourly fee for 1-hour bus should be $20";

        // Strategy 2: Flat rate (swap at runtime)
        FeeCalculationStrategy flatStrategy = new FlatRateFeeStrategy();
        double flatFee = flatStrategy.calculateFee(duration, bus.getVehicleType());
        assert flatFee == 20.0 : "Flat fee for bus should be $20";

        // Strategy 3: Switch back to hourly for longer duration
        long longDuration = 120; // 2 hours
        double longHourlyFee = hourlyStrategy.calculateFee(longDuration, bus.getVehicleType());
        double longFlatFee = flatStrategy.calculateFee(longDuration, bus.getVehicleType());

        assert longHourlyFee == 40.0 : "Hourly fee for 2-hour bus should be $40";
        assert longFlatFee == 20.0 : "Flat fee for 2-hour bus should still be $20";

        System.out.println("  Strategy comparison for 2-hour BUS:");
        System.out.println("    - Hourly Strategy: $" + longHourlyFee);
        System.out.println("    - Flat Rate Strategy: $" + longFlatFee);
        System.out.println("  ✓ Runtime strategy substitution tests passed\n");
    }
}

