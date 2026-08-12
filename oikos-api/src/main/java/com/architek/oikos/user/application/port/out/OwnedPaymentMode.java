package com.architek.oikos.user.application.port.out;

/**
 * Local copy of installment.domain.valueobject.PaymentMode for this port-out DTO - same
 * rationale as OwnedInstallmentStatus: a port-out type never references another module's
 * domain layer directly (rule 4).
 */
public enum OwnedPaymentMode {
    BANK_TRANSFER,
    CASH,
    CHECK,
    DIRECT_DEBIT
}
