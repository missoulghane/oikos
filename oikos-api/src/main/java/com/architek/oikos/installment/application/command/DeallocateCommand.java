package com.architek.oikos.installment.application.command;

import com.architek.oikos.installment.domain.valueobject.AllocationId;

public record DeallocateCommand(AllocationId id) {
}
