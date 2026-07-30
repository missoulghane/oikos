package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.RecordOwnerPaymentCommand;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;

public interface RecordOwnerPaymentUseCase {

    UnitAccountMovementId record(RecordOwnerPaymentCommand command);
}
