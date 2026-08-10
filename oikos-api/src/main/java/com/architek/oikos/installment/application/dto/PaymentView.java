package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.installment.domain.model.Payment;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.installment.domain.valueobject.PaymentMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record PaymentView(PaymentId id, EntityId propertyId, EntityId unitId, PaymentMode mode, LocalDate valueDate,
                           BigDecimal amount, EntityId journalEntryId) {

    public static PaymentView from(Payment payment) {
        return new PaymentView(payment.getId(), payment.getPropertyId(), payment.getUnitId(), payment.getMode(),
                payment.getValueDate(), payment.getAmount().value(), payment.getJournalEntryId());
    }
}
