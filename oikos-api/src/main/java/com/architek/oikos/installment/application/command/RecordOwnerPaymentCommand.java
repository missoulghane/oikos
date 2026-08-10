package com.architek.oikos.installment.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.installment.domain.valueobject.PaymentMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RecordOwnerPaymentCommand(EntityId propertyId, EntityId unitId, PaymentMode mode,
                                         EntityId treasuryAccountId, LocalDate valueDate, BigDecimal amount,
                                         EntityId createdByUserId) {
}
