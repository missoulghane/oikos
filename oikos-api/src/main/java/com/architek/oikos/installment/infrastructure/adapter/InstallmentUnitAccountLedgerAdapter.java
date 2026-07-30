package com.architek.oikos.installment.infrastructure.adapter;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.port.in.FindUnitAccountByUnitUseCase;
import com.architek.oikos.accounting.application.port.in.RecordUnitAccountDebitUseCase;
import com.architek.oikos.installment.application.port.out.UnitAccountLedgerPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to accounting's public port-in
 * (FindUnitAccountByUnitUseCase, RecordUnitAccountDebitUseCase), never to
 * accounting's repository or domain model directly (rule 4/6). Named
 * distinctly from accounting's own adapters to avoid a Spring bean name
 * collision between same-named classes in different packages.
 */
@Component
public class InstallmentUnitAccountLedgerAdapter implements UnitAccountLedgerPort {

    private final FindUnitAccountByUnitUseCase findUnitAccountByUnitUseCase;
    private final RecordUnitAccountDebitUseCase recordUnitAccountDebitUseCase;

    public InstallmentUnitAccountLedgerAdapter(FindUnitAccountByUnitUseCase findUnitAccountByUnitUseCase,
                                                RecordUnitAccountDebitUseCase recordUnitAccountDebitUseCase) {
        this.findUnitAccountByUnitUseCase = findUnitAccountByUnitUseCase;
        this.recordUnitAccountDebitUseCase = recordUnitAccountDebitUseCase;
    }

    @Override
    public Optional<EntityId> findUnitAccountId(EntityId unitId) {
        return findUnitAccountByUnitUseCase.findByUnitId(unitId);
    }

    @Override
    public void recordDebit(EntityId unitAccountId, BigDecimal amount, String label, EntityId installmentId) {
        recordUnitAccountDebitUseCase.recordDebit(unitAccountId, amount, label, installmentId);
    }
}
