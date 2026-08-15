package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * The payment exists but carries no receipt - generation failed, or it predates
 * the feature. Distinct from PaymentNotFoundException so the caller knows to
 * regenerate rather than to look for another payment.
 */
public class PaymentReceiptNotFoundException extends ResourceNotFoundException {

    public PaymentReceiptNotFoundException(String paymentId) {
        super("No receipt attached to payment: " + paymentId);
    }
}
