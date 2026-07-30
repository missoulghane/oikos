package com.architek.oikos.accounting.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.RecordOwnerPaymentCommand;
import com.architek.oikos.accounting.application.port.in.RecordOwnerPaymentUseCase;
import com.architek.oikos.accounting.domain.exception.FinancialAccountNotFoundException;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.model.FinancialJournalEntry;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.repository.FinancialJournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.valueobject.Amount;

/**
 * Spec &sect;11: a payment always impacts BOTH the treasury (a real IN entry
 * on the chosen financial account) AND the unit account (a CREDIT movement),
 * same transaction. If the payment exceeds the unit's debt, its balance
 * simply becomes positive - an available advance (no separate "advance"
 * movement type is needed, the balance already expresses it).
 */
@Component
public class RecordOwnerPaymentService implements RecordOwnerPaymentUseCase {

    private static final String PAYMENT_MOVEMENT_LABEL = "Paiement propriétaire";

    private final UnitAccountRepository unitAccountRepository;
    private final UnitAccountMovementRepository unitAccountMovementRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final FinancialJournalEntryRepository financialJournalEntryRepository;
    private final EnforceExerciseOpenService enforceExerciseOpenService;
    private final Clock clock;

    public RecordOwnerPaymentService(UnitAccountRepository unitAccountRepository,
                                      UnitAccountMovementRepository unitAccountMovementRepository,
                                      FinancialAccountRepository financialAccountRepository,
                                      FinancialJournalEntryRepository financialJournalEntryRepository,
                                      EnforceExerciseOpenService enforceExerciseOpenService, Clock clock) {
        this.unitAccountRepository = unitAccountRepository;
        this.unitAccountMovementRepository = unitAccountMovementRepository;
        this.financialAccountRepository = financialAccountRepository;
        this.financialJournalEntryRepository = financialJournalEntryRepository;
        this.enforceExerciseOpenService = enforceExerciseOpenService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UnitAccountMovementId record(RecordOwnerPaymentCommand command) {
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExercise(command.propertyId());
        UnitAccount unitAccount = unitAccountRepository.findByUnitId(command.unitId())
                .orElseThrow(() -> UnitAccountNotFoundException.forUnit(command.unitId()));
        FinancialAccount financialAccount = financialAccountRepository.findById(command.financialAccountId())
                .filter(account -> account.getPropertyId().equals(command.propertyId()))
                .orElseThrow(() -> new FinancialAccountNotFoundException(command.financialAccountId()));

        Amount amount = Amount.of(command.amount());

        FinancialJournalEntry entry = FinancialJournalEntry.create(FinancialJournalEntryId.newId(), exercise.getId(),
                financialAccount.getId(), command.date(), FinancialEntryType.OWNER_PAYMENT,
                FinancialEntryDirection.IN, amount, command.label(), command.unitId().toString(),
                command.createdByUserId());
        financialJournalEntryRepository.save(entry);
        financialAccountRepository.save(financialAccount.applyEntry(FinancialEntryDirection.IN, command.amount()));

        UnitAccountMovement movement = UnitAccountMovement.create(UnitAccountMovementId.newId(), exercise.getId(),
                unitAccount.getId(), command.date(), UnitAccountMovementType.PAYMENT,
                UnitAccountMovementDirection.CREDIT, amount, null, PAYMENT_MOVEMENT_LABEL, null);
        UnitAccountMovement savedMovement = unitAccountMovementRepository.save(movement);
        unitAccountRepository.save(unitAccount.applyMovement(UnitAccountMovementDirection.CREDIT, command.amount(),
                clock.instant()));

        return savedMovement.getId();
    }
}
