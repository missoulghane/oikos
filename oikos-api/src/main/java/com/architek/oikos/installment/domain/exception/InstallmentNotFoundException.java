package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class InstallmentNotFoundException extends ResourceNotFoundException {

    public InstallmentNotFoundException(InstallmentId id) {
        super("Installment not found with id: " + id);
    }
}
