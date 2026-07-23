package com.architek.oikos.installment.application.command;

import java.math.BigDecimal;

import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AllocatePaymentCommand(EntityId movementId, InstallmentId installmentId, BigDecimal amount) {
}
