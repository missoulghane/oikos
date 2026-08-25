package com.architek.oikos.installment.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.event.PaymentRecordedEvent;
import com.architek.oikos.installment.application.port.in.RegularizeUnitInstallmentsUseCase;
import com.architek.oikos.installment.domain.exception.NothingToRegularizeException;

/**
 * Settles what a lot's advance can settle as soon as a recette is recorded on
 * that lot, rather than leaving it to the nightly sweep
 * (RegularizeDueInstallmentsService): recording a recette is precisely the
 * moment the lot's position is looked at.
 *
 * The gap this closes: PaymentAllocationCalculator only ever imputes the money
 * of the payment being recorded. An advance booked earlier - typically a
 * reglement received before the fund call it was meant for existed - stays
 * un-imputed, so a lot would show unpaid echeances already fallen due while its
 * own advance sat right next to them, until the next nightly sweep.
 *
 * AFTER_COMMIT, for two reasons. The advance is read from accounting's journal,
 * and the entry this very payment just posted has to be committed for that
 * figure to be the real remainder rather than the balance from before. And a
 * failure here must never take the payment down with it: an encaissement that
 * really happened cannot be lost because the imputation could not be posted -
 * hence the catch, loud in the logs, and the nightly sweep picks the lot up
 * again once the cause is fixed.
 *
 * The piece date is the payment's value date, not today's: the regularization
 * entry then lands in the same accounting period as the recette that triggered
 * it, and, since RegularizeUnitInstallmentsService applies the same due-date
 * cutoff as the allocation that just ran, the payment's own leftover advance
 * cannot be imputed twice - only an advance from before it can be.
 */
@Component
public class PaymentAdvanceRegularizationListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentAdvanceRegularizationListener.class);

    private final RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase;

    public PaymentAdvanceRegularizationListener(RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase) {
        this.regularizeUnitInstallmentsUseCase = regularizeUnitInstallmentsUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentRecorded(PaymentRecordedEvent event) {
        try {
            regularizeUnitInstallmentsUseCase.regularize(new RegularizeUnitInstallmentsCommand(event.propertyId(),
                    event.unitId(), event.valueDate(), event.recordedByUserId()));
        } catch (NothingToRegularizeException nothingLeftToImpute) {
            // No advance left on the lot, or nothing due left to impute it on -
            // the ordinary outcome of a payment that covered exactly what was owed.
        } catch (RuntimeException e) {
            log.error("Advance regularization failed for unit {} after payment {} - the payment itself stands, "
                    + "regularize from the lot or the property when the cause is fixed", event.unitId(),
                    event.paymentId(), e);
        }
    }
}
