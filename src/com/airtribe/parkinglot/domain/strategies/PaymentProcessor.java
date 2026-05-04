package com.airtribe.parkinglot.domain.strategies;

import com.airtribe.parkinglot.domain.models.Receipt;

/**
 * Interface for payment processing.
 * Different payment processors implement this interface to handle payments through different channels.
 */
public interface PaymentProcessor {

    /**
     * Processes a payment for a parking ticket.
     *
     * @param ticketId the ID of the parking ticket
     * @param amount the amount to be charged
     * @return a Receipt if the payment is successful, null if payment fails
     */
    Receipt processPayment(String ticketId, double amount);

    /**
     * Returns the name of this payment processor.
     *
     * @return the processor name
     */
    String getProcessorName();
}

