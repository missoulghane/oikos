package com.architek.oikos.installment.infrastructure.receipt;

/**
 * Unchecked wrapper for a failure of the templating/PDF pipeline. Not a
 * BusinessException: a broken template or a renderer error is an infrastructure
 * problem, not an invalid request (same treatment as FileStorageException).
 *
 * Callers generating a receipt as a side effect of a payment catch this rather
 * than letting it surface - the payment is already recorded and must survive a
 * layout failure (see GeneratePaymentReceiptService).
 */
public class PaymentReceiptRenderingException extends RuntimeException {

    public PaymentReceiptRenderingException(String message, Throwable cause) {
        super(message, cause);
    }
}
