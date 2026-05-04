package com.airtribe.parkinglot.domain.models;

import com.airtribe.parkinglot.domain.enums.TicketStatus;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a parking ticket issued when a vehicle enters the parking lot.
 * A ticket tracks the vehicle, parking spot, entry/exit times, and status.
 */
public class ParkingTicket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot parkingSpot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private TicketStatus status;
    private double amountDue;

    public ParkingTicket(String ticketId, Vehicle vehicle, ParkingSpot parkingSpot, LocalDateTime entryTime) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket ID cannot be null or empty");
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        if (parkingSpot == null) {
            throw new IllegalArgumentException("Parking spot cannot be null");
        }
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time cannot be null");
        }

        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.entryTime = entryTime;
        this.exitTime = null;
        this.status = TicketStatus.ACTIVE;
        this.amountDue = 0.0;
    }

    /**
     * Completes the ticket with the exit time and calculated fee.
     * The fee is passed as a parameter to keep the ticket decoupled from fee calculation logic.
     *
     * @param exitTime the exit time
     * @param calculatedFee the fee amount calculated by the fee strategy
     * @throws IllegalStateException if the ticket is not active
     */
    public void completeTicket(LocalDateTime exitTime, double calculatedFee) {
        if (this.status != TicketStatus.ACTIVE) {
            throw new IllegalStateException("Ticket is not active. Current status: " + status);
        }
        if (exitTime == null) {
            throw new IllegalArgumentException("Exit time cannot be null");
        }
        if (exitTime.isBefore(this.entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
        if (calculatedFee < 0) {
            throw new IllegalArgumentException("Fee cannot be negative");
        }

        this.exitTime = exitTime;
        this.amountDue = calculatedFee;
        this.status = TicketStatus.COMPLETED;
    }

    /**
     * Marks the ticket as paid.
     *
     * @throws IllegalStateException if the ticket is not completed
     */
    public void markAsPaid() {
        if (this.status != TicketStatus.COMPLETED) {
            throw new IllegalStateException("Only completed tickets can be marked as paid. Current status: " + status);
        }
        this.status = TicketStatus.PAID;
    }

    /**
     * Calculates the duration of parking in minutes.
     *
     * @return the duration in minutes, or 0 if the ticket is not completed
     */
    public long getDurationInMinutes() {
        if (exitTime == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.MINUTES.between(entryTime, exitTime);
    }

    /**
     * Calculates the duration of parking in hours (rounded).
     *
     * @return the duration in hours, or 0 if the ticket is not completed
     */
    public long getDurationInHours() {
        long durationInMinutes = getDurationInMinutes();
        return (durationInMinutes + 59) / 60; // Round up
    }

    // Getters
    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public double getAmountDue() {
        return amountDue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingTicket that = (ParkingTicket) o;
        return Objects.equals(ticketId, that.ticketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId);
    }

    @Override
    public String toString() {
        return "ParkingTicket{" +
                "ticketId='" + ticketId + '\'' +
                ", vehicle=" + vehicle +
                ", parkingSpot=" + parkingSpot.getSpotId() +
                ", entryTime=" + entryTime +
                ", exitTime=" + exitTime +
                ", status=" + status +
                ", amountDue=" + amountDue +
                '}';
    }
}

