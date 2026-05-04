package com.airtribe.parkinglot.services;

import com.airtribe.parkinglot.domain.enums.TicketStatus;
import com.airtribe.parkinglot.domain.models.*;
import com.airtribe.parkinglot.domain.strategies.FeeCalculationStrategy;
import com.airtribe.parkinglot.domain.strategies.PaymentProcessor;
import com.airtribe.parkinglot.domain.strategies.SpotAllocationStrategy;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ParkingLotManager orchestrates the parking lot operations.
 * It coordinates between floors, spots, tickets, fees, and payments.
 * This is the main service layer for vehicle entry/exit operations.
 *
 * Thread-safe implementation ensures concurrent entry/exit without spot conflicts.
 */
public class ParkingLotManager {
    private final ParkingLot parkingLot;
    private volatile SpotAllocationStrategy allocationStrategy;
    private volatile FeeCalculationStrategy feeCalculationStrategy;
    private final PaymentProcessor paymentProcessor;
    private final ConcurrentHashMap<String, ParkingTicket> ticketRegistry;

    public ParkingLotManager(SpotAllocationStrategy allocationStrategy,
                           FeeCalculationStrategy feeCalculationStrategy,
                           PaymentProcessor paymentProcessor) {
        if (allocationStrategy == null) {
            throw new IllegalArgumentException("Allocation strategy cannot be null");
        }
        if (feeCalculationStrategy == null) {
            throw new IllegalArgumentException("Fee calculation strategy cannot be null");
        }
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("Payment processor cannot be null");
        }

        this.parkingLot = ParkingLot.getInstance();
        this.allocationStrategy = allocationStrategy;
        this.feeCalculationStrategy = feeCalculationStrategy;
        this.paymentProcessor = paymentProcessor;
        this.ticketRegistry = new ConcurrentHashMap<>();
    }

    /**
     * Issues a parking ticket for a vehicle entering the lot.
     * Uses the current allocation strategy to find a suitable spot.
     * This operation is thread-safe: only one vehicle gets a spot even with concurrent calls.
     *
     * @param vehicle the vehicle entering the lot
     * @return a parking ticket if a spot is available, null if the lot is full
     */
    public ParkingTicket issueTicket(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }

        // Find a spot using the current allocation strategy (thread-safe)
        ParkingSpot spot = allocationStrategy.findSpot(vehicle.getVehicleType());
        if (spot == null) {
            return null; // No available spot
        }

        // Attempt to park the vehicle (atomic operation)
        synchronized (spot) {
            if (!spot.parkVehicle(vehicle)) {
                // Another thread beat us to this spot, try again recursively
                return issueTicket(vehicle);
            }
        }

        // Create and register the ticket
        String ticketId = "TICKET-" + UUID.randomUUID().toString();
        LocalDateTime entryTime = LocalDateTime.now();
        ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, spot, entryTime);

        ticketRegistry.put(ticketId, ticket);
        return ticket;
    }

    /**
     * Processes the exit of a vehicle from the parking lot.
     * Calculates fees, processes payment, and frees the spot.
     * This operation is thread-safe.
     *
     * @param ticketId the ID of the parking ticket
     * @return a receipt if payment succeeds, null if ticket not found or payment fails
     */
    public Receipt processExit(String ticketId) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket ID cannot be null or empty");
        }

        // Retrieve the ticket
        ParkingTicket ticket = ticketRegistry.get(ticketId);
        if (ticket == null) {
            return null; // Ticket not found
        }

        // Ensure the ticket is still active
        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            return null; // Ticket already processed
        }

        synchronized (ticket) {
            // Double-check in case another thread processed the exit
            if (ticket.getStatus() != TicketStatus.ACTIVE) {
                return null;
            }

            // Calculate exit time
            LocalDateTime exitTime = LocalDateTime.now();

            // Calculate parking duration and fee
            long durationInMinutes = java.time.temporal.ChronoUnit.MINUTES
                    .between(ticket.getEntryTime(), exitTime);
            double fee = feeCalculationStrategy.calculateFee(durationInMinutes, ticket.getVehicle().getVehicleType());

            // Complete the ticket with the calculated fee
            ticket.completeTicket(exitTime, fee);

            // Process payment
            Receipt receipt = paymentProcessor.processPayment(ticketId, fee);
            if (receipt == null) {
                // Payment failed - ticket remains completed but unpaid
                return null;
            }

            // Mark ticket as paid
            ticket.markAsPaid();

            // Unpark the vehicle from the spot
            ParkingSpot spot = ticket.getParkingSpot();
            synchronized (spot) {
                spot.unparkVehicle();
            }

            return receipt;
        }
    }

    /**
     * Changes the spot allocation strategy at runtime.
     * Allows switching between different allocation strategies (e.g., Nearest → BestFit).
     *
     * @param newStrategy the new allocation strategy
     */
    public void setAllocationStrategy(SpotAllocationStrategy newStrategy) {
        if (newStrategy == null) {
            throw new IllegalArgumentException("Allocation strategy cannot be null");
        }
        this.allocationStrategy = newStrategy;
    }

    /**
     * Changes the fee calculation strategy at runtime.
     * Allows switching between different fee strategies (e.g., Hourly → FlatRate).
     *
     * @param newStrategy the new fee calculation strategy
     */
    public void setFeeCalculationStrategy(FeeCalculationStrategy newStrategy) {
        if (newStrategy == null) {
            throw new IllegalArgumentException("Fee calculation strategy cannot be null");
        }
        this.feeCalculationStrategy = newStrategy;
    }

    /**
     * Retrieves a ticket by its ID.
     *
     * @param ticketId the ticket ID
     * @return the ticket if found, null otherwise
     */
    public ParkingTicket getTicket(String ticketId) {
        return ticketRegistry.get(ticketId);
    }

    /**
     * Gets the current allocation strategy.
     *
     * @return the current allocation strategy
     */
    public SpotAllocationStrategy getAllocationStrategy() {
        return allocationStrategy;
    }

    /**
     * Gets the current fee calculation strategy.
     *
     * @return the current fee calculation strategy
     */
    public FeeCalculationStrategy getFeeCalculationStrategy() {
        return feeCalculationStrategy;
    }

    /**
     * Gets the parking lot.
     *
     * @return the parking lot instance
     */
    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    /**
     * Gets the count of active (unprocessed) tickets.
     *
     * @return the number of active tickets
     */
    public int getActiveTicketCount() {
        return (int) ticketRegistry.values().stream()
                .filter(t -> t.getStatus() == TicketStatus.ACTIVE)
                .count();
    }

    /**
     * Gets the count of all registered tickets.
     *
     * @return the number of all tickets
     */
    public int getTotalTicketCount() {
        return ticketRegistry.size();
    }

    @Override
    public String toString() {
        return "ParkingLotManager{" +
                "allocationStrategy=" + allocationStrategy.getStrategyName() +
                ", feeStrategy=" + feeCalculationStrategy.getStrategyName() +
                ", activeTickets=" + getActiveTicketCount() +
                ", totalTickets=" + getTotalTicketCount() +
                '}';
    }
}

