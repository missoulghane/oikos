package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;
import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RegularizeUnitInstallmentsResult(EntityId unitId, EntityId journalEntryId, BigDecimal amountApplied,
                                                List<InstallmentAllocationView> allocations) {
}
