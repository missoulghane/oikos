package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.ValidateUnitLettrageCommand;
import com.architek.oikos.accounting.application.port.out.InstallmentSettlementPort;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.LettrageAllocationLine;
import com.architek.oikos.accounting.domain.model.LettrageMovementLine;
import com.architek.oikos.accounting.domain.model.LettrageProposal;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.model.UnitAccountAllocation;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountAllocationRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ValidateUnitLettrageServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-10T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UnitAccountRepository unitAccountRepository;

    @Mock
    private UnitAccountAllocationRepository unitAccountAllocationRepository;

    @Mock
    private ResolveLettrageProposalService resolveLettrageProposalService;

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    @Mock
    private InstallmentSettlementPort installmentSettlementPort;

    private ValidateUnitLettrageService newService() {
        return new ValidateUnitLettrageService(unitAccountRepository, unitAccountAllocationRepository,
                resolveLettrageProposalService, enforceExerciseOpenService, installmentSettlementPort, CLOCK);
    }

    @Test
    void validating_persists_one_allocation_per_proposed_line_and_pushes_the_new_outstanding_amount() {
        EntityId unitId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        EntityId validatedByUserId = EntityId.newId();
        UnitAccountId unitAccountId = UnitAccountId.newId();
        UnitAccount unitAccount = UnitAccount.reconstruct(unitAccountId, unitId, propertyId, BigDecimal.ZERO,
                Instant.now());
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);

        String installmentId = EntityId.newId().toString();
        UnitAccountMovement debitMovement = UnitAccountMovement.create(UnitAccountMovementId.newId(),
                AccountingExerciseId.newId(), unitAccountId, LocalDate.of(2026, 3, 1), UnitAccountMovementType.FUND_CALL,
                UnitAccountMovementDirection.DEBIT, Amount.of(new BigDecimal("150")), installmentId,
                "Appel de cotisation", null);
        UnitAccountMovementId creditId = UnitAccountMovementId.newId();

        LettrageProposal proposal = new LettrageProposal(
                List.of(new LettrageMovementLine(debitMovement.getId(), debitMovement.getDate(), new BigDecimal("150"))),
                List.of(),
                List.of(new LettrageAllocationLine(debitMovement.getId(), creditId, new BigDecimal("150"))),
                BigDecimal.ZERO, BigDecimal.ZERO);

        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.of(unitAccount));
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(resolveLettrageProposalService.resolve(unitAccountId))
                .thenReturn(new LettrageContext(List.of(debitMovement), proposal));
        when(unitAccountAllocationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().validate(new ValidateUnitLettrageCommand(unitId, validatedByUserId));

        ArgumentCaptor<UnitAccountAllocation> captor = ArgumentCaptor.forClass(UnitAccountAllocation.class);
        verify(unitAccountAllocationRepository).save(captor.capture());
        UnitAccountAllocation allocation = captor.getValue();
        assertThat(allocation.getDebitMovementId()).isEqualTo(debitMovement.getId());
        assertThat(allocation.getCreditMovementId()).isEqualTo(creditId);
        assertThat(allocation.getAmount().value()).isEqualByComparingTo("150");
        assertThat(allocation.getAllocatedDate()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(allocation.getAllocatedByUserId()).isEqualTo(validatedByUserId);

        ArgumentCaptor<BigDecimal> outstandingCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(installmentSettlementPort).updateOutstandingAmount(eq(EntityId.of(installmentId)), outstandingCaptor.capture());
        assertThat(outstandingCaptor.getValue()).isEqualByComparingTo("0");
    }

    @Test
    void a_unit_with_no_account_is_rejected() {
        EntityId unitId = EntityId.newId();
        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().validate(new ValidateUnitLettrageCommand(unitId, EntityId.newId())))
                .isInstanceOf(UnitAccountNotFoundException.class);
    }
}
