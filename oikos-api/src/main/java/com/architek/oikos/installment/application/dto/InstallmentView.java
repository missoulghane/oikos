package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * unitNumber is the lot as printed ("A12"), resolved by the read use cases that
 * list across a whole property - null on the reads scoped to a single unit,
 * where the caller already knows which lot it asked for.
 */
public record InstallmentView(InstallmentId id, EntityId unitId, String unitNumber, LocalDate dueDate,
                                  BigDecimal amount, BigDecimal outstandingAmount, InstallmentStatus status,
                                  YearMonth period) {
}
