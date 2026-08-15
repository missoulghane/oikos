package com.architek.oikos.installment.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.architek.oikos.installment.application.event.PaymentRecordedEvent;
import com.architek.oikos.installment.application.port.in.GeneratePaymentReceiptUseCase;

/**
 * Generates the receipt once the payment is durably committed, and never inside
 * its transaction.
 *
 * AFTER_COMMIT matters twice over. The PDF must not be produced from a payment
 * that could still roll back; and, more importantly, a failure here must not
 * take the payment down with it - an encaissement that really happened cannot be
 * lost because a template broke or a disk filled up. Hence the catch: the
 * failure is loud in the logs, and the receipt is regenerable on demand
 * (POST /payments/{id}/receipt).
 */
@Component
public class PaymentReceiptGenerationListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentReceiptGenerationListener.class);

    private final GeneratePaymentReceiptUseCase generatePaymentReceiptUseCase;

    public PaymentReceiptGenerationListener(GeneratePaymentReceiptUseCase generatePaymentReceiptUseCase) {
        this.generatePaymentReceiptUseCase = generatePaymentReceiptUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentRecorded(PaymentRecordedEvent event) {
        try {
            generatePaymentReceiptUseCase.generate(event.paymentId());
        } catch (RuntimeException e) {
            log.error("Receipt generation failed for payment {} - the payment itself stands, "
                    + "regenerate with POST /payments/{}/receipt", event.paymentId(), event.paymentId(), e);
        }
    }
}
