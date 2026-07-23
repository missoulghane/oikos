package com.architek.oikos.installment.application.usecase;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.InstallmentCallLine;
import com.architek.oikos.installment.application.command.RecordInstallmentCallCommand;
import com.architek.oikos.installment.application.port.in.RecordInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.out.AccountLedgerPort;
import com.architek.oikos.installment.application.port.out.UnitDirectoryPort;
import com.architek.oikos.installment.domain.exception.AccountNotFoundException;
import com.architek.oikos.installment.domain.exception.UnitNotFoundException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * RG003: for every line, creates the Installment and its triggering debit
 * Movement in the same transaction, on the line's unit account (one account
 * per lot, not per co-owner). FIFO auto-allocation is then attempted for that
 * account, so an advance payment already on the account (see spec §4) settles
 * the new installment immediately. All ledger effects (movement +
 * balance/mirror) are applied through AccountLedgerPort, never accounting's
 * repositories directly (rule 4).
 */
@Component
public class RecordInstallmentCallService implements RecordInstallmentCallUseCase {

    private static final String INSTALLMENT_MOVEMENT_LABEL = "Appel de cotisation";

    private final InstallmentRepository installmentRepository;
    private final AccountLedgerPort accountLedgerPort;
    private final AutoAllocationEngine autoAllocationEngine;
    private final UnitDirectoryPort unitDirectoryPort;

    public RecordInstallmentCallService(InstallmentRepository installmentRepository, AccountLedgerPort accountLedgerPort,
                                        AutoAllocationEngine autoAllocationEngine, UnitDirectoryPort unitDirectoryPort) {
        this.installmentRepository = installmentRepository;
        this.accountLedgerPort = accountLedgerPort;
        this.autoAllocationEngine = autoAllocationEngine;
        this.unitDirectoryPort = unitDirectoryPort;
    }

    @Override
    @Transactional
    public List<InstallmentId> record(RecordInstallmentCallCommand command) {
        List<InstallmentId> createdIds = new ArrayList<>();

        for (InstallmentCallLine line : command.lines()) {
            if (!unitDirectoryPort.exists(line.unitId())) {
                throw new UnitNotFoundException(line.unitId());
            }

            EntityId accountId = accountLedgerPort.findUnitAccountId(line.unitId())
                    .orElseThrow(() -> AccountNotFoundException.forUnit(line.unitId()));

            Installment installment = Installment.create(InstallmentId.newId(), accountId, line.unitId(),
                    command.dueDate(), Amount.of(line.amount()));
            Installment savedInstallment = installmentRepository.save(installment);

            accountLedgerPort.recordDebit(accountId, line.amount(), INSTALLMENT_MOVEMENT_LABEL);
            autoAllocationEngine.allocate(accountId);

            createdIds.add(savedInstallment.getId());
        }

        return createdIds;
    }
}
