package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record InstallmentCallSummaryView(InstallmentCallId id, EntityId propertyId, YearMonth period, LocalDate dueDate,
                                            int unitCount, BigDecimal totalAmount) {
}
