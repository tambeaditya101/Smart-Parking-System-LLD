package com.airtribe.parkinglot.domain.strategies;

import com.airtribe.parkinglot.domain.enums.PaymentMethod;
import com.airtribe.parkinglot.domain.models.Receipt;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Credit card payment processor implementation.
 * Handles payment processing via credit card with basic validation.
 */
public class CreditCardPaymentProcessor implements PaymentProcessor {

    @Override
    public Receipt processPayment(String ticketId, double amount) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket ID cannot be null or empty");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        // Simulate credit card validation and processing
        // In a real system, this would integrate with a payment gateway
        if (validateCreditCard() && processTransaction(amount)) {
            // Generate a receipt ID
            String receiptId = "RECEIPT-" + UUID.randomUUID().toString();

            // Create and return the receipt
            return new Receipt(
                    receiptId,
                    ticketId,
                    amount,
                    LocalDateTime.now(),
                    PaymentMethod.CREDIT_CARD
            );
        }

        // Payment failed
        return null;
    }

    @Override
    public String getProcessorName() {
        return "Credit Card Payment Processor";
    }

    /**
     * Validates the credit card.
     * In a real system, this would check card details, expiry, etc.
     *
     * @return true if the card is valid, false otherwise
     */
    private boolean validateCreditCard() {
        // Simulate validation - in real system, validate against payment gateway
        // For now, always return true
        return true;
    }

    /**
     * Processes the transaction with the payment gateway.
     *
     * @param amount the amount to be charged
     * @return true if the transaction is successful, false otherwise
     */
    private boolean processTransaction(double amount) {
        // Simulate transaction processing
        // In a real system, this would integrate with a payment gateway (Stripe, PayPal, etc.)
        // For now, simulate success (with 99% success rate to mimic real failures)
        return Math.random() < 0.99;
    }

    @Override
    public String toString() {
        return "CreditCardPaymentProcessor{}";
    }
}

