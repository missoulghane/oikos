package com.architek.oikos.accounting.application.usecase;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.RecordUnitAccountRegularizationCommand;
import com.architek.oikos.accounting.application.port.in.RecordUnitAccountRegularizationUseCase;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.valueobject.Amount;

/** Spec &sect;13: a correction always requires a mandatory reason (enforced by UnitAccountMovement itself). */
@Component
public class RecordUnitAccountRegularizationService implements RecordUnitAccountRegularizationUseCase {

    private final UnitAccountRepository unitAccountRepository;
    private final UnitAccountMovementRepository unitAccountMovementRepository;
    private final EnforceExerciseOpenService enforceExerciseOpenService;
    private final Clock clock;

    public RecordUnitAccountRegularizationService(UnitAccountRepository unitAccountRepository,
                                                    UnitAccountMovementRepository unitAccountMovementRepository,
                                                    EnforceExerciseOpenService enforceExerciseOpenService,
                                                    Clock clock) {
        this.unitAccountRepository = unitAccountRepository;
        this.unitAccountMovementRepository = unitAccountMovementRepository;
        this.enforceExerciseOpenService = enforceExerciseOpenService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UnitAccountMovementId record(RecordUnitAccountRegularizationCommand command) {
        UnitAccount unitAccount = unitAccountRepository.findByUnitId(command.unitId())
                .orElseThrow(() -> UnitAccountNotFoundException.forUnit(command.unitId()));
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExercise(unitAccount.getPropertyId());

        LocalDate today = LocalDate.now(clock);
        UnitAccountMovement movement = UnitAccountMovement.create(UnitAccountMovementId.newId(), exercise.getId(),
                unitAccount.getId(), today, UnitAccountMovementType.REGULARIZATION, command.direction(),
                Amount.of(command.amount()), null, command.label(), command.reason());
        UnitAccountMovement savedMovement = unitAccountMovementRepository.save(movement);

        unitAccountRepository.save(
                unitAccount.applyMovement(command.direction(), command.amount(), clock.instant()));

        return savedMovement.getId();
    }
}
