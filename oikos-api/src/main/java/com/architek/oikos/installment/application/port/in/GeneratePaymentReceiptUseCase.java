package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Produces the receipt PDF of a payment and attaches it to that payment (as a
 * Document with ownerType=PAYMENT). Idempotent from the caller's point of view:
 * an existing receipt is replaced, and the payment's reference is never renumbered.
 *
 * generatedByUserId must be an existing app_user - the one who recorded the
 * payment, or the one asking for a reprint. The document module stores it in
 * document.uploaded_by, which is a foreign key onto app_user.
 */
public interface GeneratePaymentReceiptUseCase {

    void generate(PaymentId paymentId, EntityId generatedByUserId);
}
