package com.architek.oikos.installment.application.port.in;

import java.util.List;

import com.architek.oikos.installment.application.command.RecordInstallmentCallCommand;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;

public interface RecordInstallmentCallUseCase {

    List<InstallmentId> record(RecordInstallmentCallCommand command);
}
