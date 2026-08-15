package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.domain.valueobject.PaymentId;

/**
 * Produces the receipt PDF of a payment and attaches it to that payment (as a
 * Document with ownerType=PAYMENT). Idempotent from the caller's point of view:
 * an existing receipt is replaced, and the payment's reference is never renumbered.
 */
public interface GeneratePaymentReceiptUseCase {

    void generate(PaymentId paymentId);
}
