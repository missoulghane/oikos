package com.architek.oikos.user.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record OwnedPaymentView(EntityId id, EntityId propertyId, EntityId unitId, OwnedPaymentMode mode,
                                LocalDate valueDate, BigDecimal amount, EntityId journalEntryId) {
}
