package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.exception.BusinessException;

public class AllocationExceedsInstallmentDueException extends BusinessException {

    public AllocationExceedsInstallmentDueException(InstallmentId installmentId) {
        super("Allocation amount exceeds the remaining due amount of installment " + installmentId);
    }
}
