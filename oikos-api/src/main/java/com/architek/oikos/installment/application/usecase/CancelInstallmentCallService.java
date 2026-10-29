package com.architek.oikos.installment.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.CancelInstallmentCallCommand;
import com.architek.oikos.installment.application.port.in.CancelInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.out.FundCallJournalEntryPort;
import com.architek.oikos.installment.domain.exception.InstallmentCallNotFoundException;
import com.architek.oikos.installment.domain.exception.InstallmentCallSettledException;
import com.architek.oikos.installment.domain.exception.InvalidInstallmentCallTransitionException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallStatus;

/**
 * Cancels a fund call. A posted call is never deleted: its VT entry is
 * reversed (contre-passation, accounting's P10), the call is kept as
 * CANCELLED with its journalEntryId as the audit trail, and only its
 * installments go - they were never paid, so nothing references them.
 *
 * <p>Refused as soon as one installment has been settled, even partly: the
 * payment or advance imputed on it would have to be turned back into an
 * advance first, which is a story of its own.
 *
 * <p>A cancelled call frees its period: the same month can be generated again
 * (the uniqueness check and the database index both ignore CANCELLED calls).
 */
@Component
public class CancelInstallmentCallService implements CancelInstallmentCallUseCase {

    private final InstallmentCallRepository installmentCallRepository;
    private final InstallmentRepository installmentRepository;
    private final FundCallJournalEntryPort fundCallJournalEntryPort;
    private final Clock clock;

    public CancelInstallmentCallService(InstallmentCallRepository installmentCallRepository,
                                         InstallmentRepository installmentRepository,
                                         FundCallJournalEntryPort fundCallJournalEntryPort,
                                         Clock clock) {
        this.installmentCallRepository = installmentCallRepository;
        this.installmentRepository = installmentRepository;
        this.fundCallJournalEntryPort = fundCallJournalEntryPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void cancel(CancelInstallmentCallCommand command) {
        InstallmentCall call = installmentCallRepository.findById(command.id())
                .orElseThrow(() -> new InstallmentCallNotFoundException(command.id()));
        if (call.isCancelled()) {
            throw new InvalidInstallmentCallTransitionException(call.getId(), call.getStatus(), "be cancelled again");
        }

        List<Installment> installments = installmentRepository.findAllByInstallmentCallId(call.getId());
        boolean anySettled = installments.stream()
                .anyMatch(installment -> installment.getOutstandingAmount()
                        .compareTo(installment.getAmount().value()) != 0);
        if (anySettled) {
            throw new InstallmentCallSettledException(call.getId());
        }

        InstallmentCall cancelled;
        if (call.getStatus() == InstallmentCallStatus.POSTED) {
            call.getJournalEntryId().ifPresent(journalEntryId -> fundCallJournalEntryPort.reverseFundCallEntry(
                    call.getPropertyId(), journalEntryId, LocalDate.now(clock), command.cancelledByUserId()));
            cancelled = call.cancelPosted();
        } else {
            cancelled = call.cancel();
        }

        installmentRepository.deleteAllByInstallmentCallId(call.getId());
        installmentCallRepository.save(cancelled);
    }
}
