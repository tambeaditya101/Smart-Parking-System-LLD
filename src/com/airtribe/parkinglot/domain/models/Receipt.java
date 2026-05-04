package com.airtribe.parkinglot.domain.models;

import com.airtribe.parkinglot.domain.enums.PaymentMethod;
import com.airtribe.parkinglot.domain.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a receipt for a parking payment transaction.
 * Contains details about the payment including the ticket, amount, and status.
 */
public class Receipt {
    private final String receiptId;
    private final String ticketId;
    private final double amount;
    private final LocalDateTime paymentTime;
    private final PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    public Receipt(String receiptId, String ticketId, double amount, LocalDateTime paymentTime, PaymentMethod paymentMethod) {
        if (receiptId == null || receiptId.trim().isEmpty()) {
            throw new IllegalArgumentException("Receipt ID cannot be null or empty");
        }
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket ID cannot be null or empty");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (paymentTime == null) {
            throw new IllegalArgumentException("Payment time cannot be null");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }

        this.receiptId = receiptId;
        this.ticketId = ticketId;
        this.amount = amount;
        this.paymentTime = paymentTime;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.COMPLETED;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new IllegalArgumentException("Payment status cannot be null");
        }
        this.paymentStatus = paymentStatus;
    }

    /**
     * Formats the receipt as a readable string.
     *
     * @return formatted receipt details
     */
    public String formatReceipt() {
        return "========== PARKING RECEIPT ==========\n" +
                "Receipt ID: " + receiptId + "\n" +
                "Ticket ID: " + ticketId + "\n" +
                "Amount: $" + String.format("%.2f", amount) + "\n" +
                "Payment Method: " + paymentMethod.name() + "\n" +
                "Payment Time: " + paymentTime + "\n" +
                "Status: " + paymentStatus.name() + "\n" +
                "=====================================";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return Objects.equals(receiptId, receipt.receiptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptId);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "receiptId='" + receiptId + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", amount=" + amount +
                ", paymentTime=" + paymentTime +
                ", paymentMethod=" + paymentMethod +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}

