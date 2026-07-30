package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/** Spec &sect;16 "Units": total du / total paye / avance disponible / solde actuel, per unit. */
public record UnitAccountSummaryView(EntityId unitId, BigDecimal totalDue, BigDecimal totalPaid,
                                      BigDecimal availableAdvance, BigDecimal currentBalance) {
}
