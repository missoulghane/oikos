package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.ValidateUnitLettrageCommand;
import com.architek.oikos.accounting.application.port.in.ValidateUnitLettrageUseCase;
import com.architek.oikos.accounting.application.port.out.InstallmentSettlementPort;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.LettrageAllocationLine;
import com.architek.oikos.accounting.domain.model.LettrageMovementLine;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.model.UnitAccountAllocation;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountAllocationRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountAllocationId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Commits the current FIFO proposal as-is - recomputed at this instant from
 * the movement/allocation history (no client-supplied payload) so what gets
 * persisted always matches what the review screen showed a moment ago. Every
 * fund call touched by the newly persisted lines has its installment
 * outstanding amount pushed to installment (see InstallmentSettlementPort)
 * so its status (Non soldee/Partiellement soldee/Soldee) stays in sync.
 */
@Component
public class ValidateUnitLettrageService implements ValidateUnitLettrageUseCase {

    private final UnitAccountRepository unitAccountRepository;
    private final UnitAccountAllocationRepository unitAccountAllocationRepository;
    private final ResolveLettrageProposalService resolveLettrageProposalService;
    private final EnforceExerciseOpenService enforceExerciseOpenService;
    private final InstallmentSettlementPort installmentSettlementPort;
    private final Clock clock;

    public ValidateUnitLettrageService(UnitAccountRepository unitAccountRepository,
                                        UnitAccountAllocationRepository unitAccountAllocationRepository,
                                        ResolveLettrageProposalService resolveLettrageProposalService,
                                        EnforceExerciseOpenService enforceExerciseOpenService,
                                        InstallmentSettlementPort installmentSettlementPort, Clock clock) {
        this.unitAccountRepository = unitAccountRepository;
        this.unitAccountAllocationRepository = unitAccountAllocationRepository;
        this.resolveLettrageProposalService = resolveLettrageProposalService;
        this.enforceExerciseOpenService = enforceExerciseOpenService;
        this.installmentSettlementPort = installmentSettlementPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void validate(ValidateUnitLettrageCommand command) {
        UnitAccount unitAccount = unitAccountRepository.findByUnitId(command.unitId())
                .orElseThrow(() -> UnitAccountNotFoundException.forUnit(command.unitId()));
        enforceExerciseOpenService.requireOpenExercise(unitAccount.getPropertyId());

        LettrageContext context = resolveLettrageProposalService.resolve(unitAccount.getId());
        LocalDate today = LocalDate.now(clock);

        Map<UnitAccountMovementId, BigDecimal> consumedPerDebit = context.proposal().lines().stream()
                .collect(Collectors.groupingBy(LettrageAllocationLine::debitMovementId,
                        Collectors.reducing(BigDecimal.ZERO, LettrageAllocationLine::amount, BigDecimal::add)));

        for (LettrageAllocationLine line : context.proposal().lines()) {
            UnitAccountAllocation allocation = UnitAccountAllocation.create(UnitAccountAllocationId.newId(),
                    unitAccount.getId(), line.debitMovementId(), line.creditMovementId(), Amount.of(line.amount()),
                    today, command.validatedByUserId());
            unitAccountAllocationRepository.save(allocation);
        }

        pushSettlementUpdates(context, consumedPerDebit);
    }

    private void pushSettlementUpdates(LettrageContext context, Map<UnitAccountMovementId, BigDecimal> consumedPerDebit) {
        if (consumedPerDebit.isEmpty()) {
            return;
        }

        Map<UnitAccountMovementId, BigDecimal> outstandingBeforeByDebit = context.proposal().unsettledDebits().stream()
                .collect(Collectors.toMap(LettrageMovementLine::movementId, LettrageMovementLine::amount));
        Map<UnitAccountMovementId, UnitAccountMovement> movementsById = context.movements().stream()
                .collect(Collectors.toMap(UnitAccountMovement::getId, Function.identity()));

        consumedPerDebit.forEach((debitMovementId, consumed) -> {
            BigDecimal outstandingAfter = outstandingBeforeByDebit.get(debitMovementId).subtract(consumed);
            String installmentId = movementsById.get(debitMovementId).getBusinessReference();
            installmentSettlementPort.updateOutstandingAmount(EntityId.of(installmentId), outstandingAfter);
        });
    }
}
