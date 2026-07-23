package com.architek.oikos.installment.application.dto;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record GenerateInstallmentCallResult(InstallmentCallView installmentCall, List<EntityId> chargedUnitIds,
                                               List<EntityId> skippedUnitIds) {
}
