package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.port.in.RecordUnitAccountDebitUseCase;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public entry point for installment's UnitAccountLedgerPort (spec &sect;10):
 * posts the FUND_CALL debit triggered by raising an Installment, in the same
 * transaction as the caller's own Installment creation.
 */
@Component
public class RecordUnitAccountDebitService implements RecordUnitAccountDebitUseCase {

    private static final String FUND_CALL_MOVEMENT_LABEL = "Appel de cotisation";

    private final UnitAccountRepository unitAccountRepository;
    private final UnitAccountMovementRepository unitAccountMovementRepository;
    private final EnforceExerciseOpenService enforceExerciseOpenService;
    private final Clock clock;

    public RecordUnitAccountDebitService(UnitAccountRepository unitAccountRepository,
                                          UnitAccountMovementRepository unitAccountMovementRepository,
                                          EnforceExerciseOpenService enforceExerciseOpenService, Clock clock) {
        this.unitAccountRepository = unitAccountRepository;
        this.unitAccountMovementRepository = unitAccountMovementRepository;
        this.enforceExerciseOpenService = enforceExerciseOpenService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void recordDebit(EntityId unitAccountId, BigDecimal amount, String label, EntityId installmentId) {
        UnitAccountId id = new UnitAccountId(unitAccountId);
        UnitAccount unitAccount = unitAccountRepository.findById(id)
                .orElseThrow(() -> new UnitAccountNotFoundException(id));
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExercise(unitAccount.getPropertyId());

        LocalDate today = LocalDate.now(clock);
        String movementLabel = label != null && !label.isBlank() ? label : FUND_CALL_MOVEMENT_LABEL;
        UnitAccountMovement movement = UnitAccountMovement.create(UnitAccountMovementId.newId(), exercise.getId(),
                unitAccount.getId(), today, UnitAccountMovementType.FUND_CALL, UnitAccountMovementDirection.DEBIT,
                Amount.of(amount), installmentId.toString(), movementLabel, null);
        unitAccountMovementRepository.save(movement);

        unitAccountRepository.save(
                unitAccount.applyMovement(UnitAccountMovementDirection.DEBIT, amount, clock.instant()));
    }
}
