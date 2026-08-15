package com.architek.oikos.installment.application.event;

import com.architek.oikos.installment.domain.valueobject.PaymentId;

/** Published when a payment is committed; carries only its id - listeners re-read what they need. */
public record PaymentRecordedEvent(PaymentId paymentId) {
}
