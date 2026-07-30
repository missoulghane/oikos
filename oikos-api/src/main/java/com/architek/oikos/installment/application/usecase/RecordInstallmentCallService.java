package com.architek.oikos.installment.application.usecase;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.InstallmentCallLine;
import com.architek.oikos.installment.application.command.RecordInstallmentCallCommand;
import com.architek.oikos.installment.application.port.in.RecordInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.out.UnitAccountLedgerPort;
import com.architek.oikos.installment.application.port.out.UnitDirectoryPort;
import com.architek.oikos.installment.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.installment.domain.exception.UnitNotFoundException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * RG003: for every line, creates the Installment on the line's unit (one
 * account per lot, not per co-owner).
 */
@Component
public class RecordInstallmentCallService implements RecordInstallmentCallUseCase {

    private static final String FUND_CALL_MOVEMENT_LABEL = "Appel de cotisation";

    private final InstallmentRepository installmentRepository;
    private final UnitDirectoryPort unitDirectoryPort;
    private final UnitAccountLedgerPort unitAccountLedgerPort;

    public RecordInstallmentCallService(InstallmentRepository installmentRepository, UnitDirectoryPort unitDirectoryPort,
                                         UnitAccountLedgerPort unitAccountLedgerPort) {
        this.installmentRepository = installmentRepository;
        this.unitDirectoryPort = unitDirectoryPort;
        this.unitAccountLedgerPort = unitAccountLedgerPort;
    }

    @Override
    @Transactional
    public List<InstallmentId> record(RecordInstallmentCallCommand command) {
        List<InstallmentId> createdIds = new ArrayList<>();

        for (InstallmentCallLine line : command.lines()) {
            if (!unitDirectoryPort.exists(line.unitId())) {
                throw new UnitNotFoundException(line.unitId());
            }

            Installment installment = Installment.create(InstallmentId.newId(), line.unitId(),
                    command.dueDate(), Amount.of(line.amount()));
            Installment savedInstallment = installmentRepository.save(installment);

            EntityId unitAccountId = unitAccountLedgerPort.findUnitAccountId(line.unitId())
                    .orElseThrow(() -> new UnitAccountNotFoundException(line.unitId()));
            unitAccountLedgerPort.recordDebit(unitAccountId, line.amount(), FUND_CALL_MOVEMENT_LABEL,
                    savedInstallment.getId().value());

            createdIds.add(savedInstallment.getId());
        }

        return createdIds;
    }
}
