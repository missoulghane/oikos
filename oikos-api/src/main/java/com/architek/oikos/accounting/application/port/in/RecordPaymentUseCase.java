package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.RecordPaymentCommand;
import com.architek.oikos.accounting.domain.valueobject.MovementId;

public interface RecordPaymentUseCase {

    MovementId record(RecordPaymentCommand command);
}
