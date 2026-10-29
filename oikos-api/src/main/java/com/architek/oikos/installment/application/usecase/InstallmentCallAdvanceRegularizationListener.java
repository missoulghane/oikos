package com.architek.oikos.installment.application.usecase;

import java.time.Clock;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.event.InstallmentCallGeneratedEvent;
import com.architek.oikos.installment.application.port.in.RegularizeUnitInstallmentsUseCase;
import com.architek.oikos.installment.domain.exception.NothingToRegularizeException;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Once a fund call is committed, imputes the advance each charged lot already
 * holds - dated today, so only what is due today is imputed: a call generated
 * for a period already started (or late) is settled from the advance at once,
 * a call not yet due is left to the daily sweep (RegularizeAdvancesUseCase).
 *
 * <p>AFTER_COMMIT and a catch-all for the same reasons as
 * PaymentAdvanceRegularizationListener: the call's VT entry has to be
 * committed first, and the call itself must stand even if an imputation fails.
 */
@Component
public class InstallmentCallAdvanceRegularizationListener {

    private static final Logger log = LoggerFactory.getLogger(InstallmentCallAdvanceRegularizationListener.class);

    private final RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase;
    private final Clock clock;

    public InstallmentCallAdvanceRegularizationListener(
            RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase, Clock clock) {
        this.regularizeUnitInstallmentsUseCase = regularizeUnitInstallmentsUseCase;
        this.clock = clock;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInstallmentCallGenerated(InstallmentCallGeneratedEvent event) {
        LocalDate today = LocalDate.now(clock);
        for (EntityId unitId : event.chargedUnitIds()) {
            try {
                regularizeUnitInstallmentsUseCase.regularize(new RegularizeUnitInstallmentsCommand(event.propertyId(),
                        unitId, today, event.generatedByUserId()));
            } catch (NothingToRegularizeException nothingLeftToImpute) {
                // No advance on the lot, or the new call is not due yet.
            } catch (RuntimeException e) {
                log.error("Advance regularization failed for unit {} after installment call {} - the call itself "
                        + "stands, the daily sweep will retry", unitId, event.installmentCallId(), e);
            }
        }
    }
}
