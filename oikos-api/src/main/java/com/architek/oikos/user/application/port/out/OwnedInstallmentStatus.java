package com.architek.oikos.user.application.port.out;

/**
 * Local copy of installment.domain.valueobject.InstallmentStatus for this port-out DTO - same
 * rationale as property/installment each keeping their own DuesCalculationMode: a port-out type
 * never references another module's domain layer directly (rule 4).
 */
public enum OwnedInstallmentStatus {
    NOT_SETTLED,
    PARTIALLY_SETTLED,
    SETTLED
}
