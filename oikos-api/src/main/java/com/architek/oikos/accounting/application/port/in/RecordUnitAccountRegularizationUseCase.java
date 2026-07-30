package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.RecordUnitAccountRegularizationCommand;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;

public interface RecordUnitAccountRegularizationUseCase {

    UnitAccountMovementId record(RecordUnitAccountRegularizationCommand command);
}
