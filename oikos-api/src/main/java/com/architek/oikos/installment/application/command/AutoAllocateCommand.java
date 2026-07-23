package com.architek.oikos.installment.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AutoAllocateCommand(EntityId accountId) {
}
