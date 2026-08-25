package com.architek.oikos.installment.application.event;

import java.time.LocalDate;

import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Published when a payment is committed. Carries its id - a listener opening its
 * own transaction re-reads whatever else it needs from there - plus the few
 * fields a listener needs <em>before</em> it can open one, and the user who
 * recorded it.
 *
 * The lot, its property and the value date are on the event for that reason:
 * the regularization listener has to name the unit to start its transaction on,
 * and re-reading the payment from the after-commit phase would mean querying
 * through a transaction that is already completing (see
 * GeneratePaymentReceiptService for what that costs on the write side).
 * The recording user is on it because it is not re-readable at all - the payment
 * stores no author, yet the receipt document has to be attributed to a real
 * app_user: document.uploaded_by is a foreign key.
 */
public record PaymentRecordedEvent(PaymentId paymentId, EntityId propertyId, EntityId unitId, LocalDate valueDate,
                                    EntityId recordedByUserId) {
}
