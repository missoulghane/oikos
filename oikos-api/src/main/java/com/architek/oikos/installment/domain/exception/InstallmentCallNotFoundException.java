package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class InstallmentCallNotFoundException extends ResourceNotFoundException {

    public InstallmentCallNotFoundException(InstallmentCallId id) {
        super("Installment call not found with id: " + id);
    }
}
