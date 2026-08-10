package com.architek.oikos.installment.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.domain.valueobject.PaymentMode;

public record PaymentResponse(String id, String propertyId, String unitId, PaymentMode mode, LocalDate valueDate,
                               BigDecimal amount, String journalEntryId) {

    public static PaymentResponse from(PaymentView view) {
        return new PaymentResponse(view.id().toString(), view.propertyId().toString(), view.unitId().toString(),
                view.mode(), view.valueDate(), view.amount(), view.journalEntryId().toString());
    }
}
