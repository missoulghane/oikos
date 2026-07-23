package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class AllocationNotFoundException extends ResourceNotFoundException {

    public AllocationNotFoundException(AllocationId id) {
        super("Allocation not found with id: " + id);
    }
}
