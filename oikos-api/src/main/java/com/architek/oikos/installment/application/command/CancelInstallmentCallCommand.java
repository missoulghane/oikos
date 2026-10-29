package com.architek.oikos.installment.application.command;

import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record CancelInstallmentCallCommand(InstallmentCallId id, EntityId cancelledByUserId) {
}
