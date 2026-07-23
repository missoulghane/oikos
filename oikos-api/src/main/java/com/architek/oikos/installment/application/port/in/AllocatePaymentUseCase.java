package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.command.AllocatePaymentCommand;
import com.architek.oikos.installment.domain.valueobject.AllocationId;

public interface AllocatePaymentUseCase {

    AllocationId allocate(AllocatePaymentCommand command);
}
