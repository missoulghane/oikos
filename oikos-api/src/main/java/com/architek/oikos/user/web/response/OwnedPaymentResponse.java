package com.architek.oikos.user.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.user.application.port.out.OwnedPaymentMode;
import com.architek.oikos.user.application.port.out.OwnedPaymentView;

public record OwnedPaymentResponse(String id, String propertyId, String unitId, OwnedPaymentMode mode,
                                    LocalDate valueDate, BigDecimal amount, String journalEntryId) {

    public static OwnedPaymentResponse from(OwnedPaymentView view) {
        return new OwnedPaymentResponse(view.id().toString(), view.propertyId().toString(), view.unitId().toString(),
                view.mode(), view.valueDate(), view.amount(), view.journalEntryId().toString());
    }
}
