package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.dto.RegularizeUnitInstallmentsResult;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Imputes lots' advances on their installments that have fallen due, dated
 * today - the triggers other than a payment (which runs
 * RegularizeUnitInstallmentsUseCase itself, dated on its value date).
 */
public interface RegularizeAdvancesUseCase {

    /** One lot, on demand - from the back-office, to retry a regularization that failed after a payment. */
    RegularizeUnitInstallmentsResult regularizeUnit(EntityId propertyId, EntityId unitId, EntityId requestedByUserId);

    /**
     * Every lot that still owes something already due: the daily sweep that
     * imputes an advance on a call which fell due after the lot's last payment.
     * Returns how many lots got an imputation.
     */
    int regularizeAllDue();
}
