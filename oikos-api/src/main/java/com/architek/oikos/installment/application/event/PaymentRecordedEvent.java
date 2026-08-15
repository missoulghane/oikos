package com.architek.oikos.installment.application.event;

import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Published when a payment is committed; carries its id - listeners re-read what
 * they need - plus the user who recorded it. That user is not re-readable from
 * the payment (it stores no author) and the after-commit listener runs outside
 * any request-bound resolution, yet the receipt document has to be attributed to
 * a real app_user: document.uploaded_by is a foreign key.
 */
public record PaymentRecordedEvent(PaymentId paymentId, EntityId recordedByUserId) {
}
