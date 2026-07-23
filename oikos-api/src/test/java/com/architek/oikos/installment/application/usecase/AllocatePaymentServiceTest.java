package com.architek.oikos.installment.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.installment.application.command.AllocatePaymentCommand;
import com.architek.oikos.installment.application.port.out.MovementInfo;
import com.architek.oikos.installment.application.port.out.MovementLookupPort;
import com.architek.oikos.installment.domain.exception.AccountMismatchException;
import com.architek.oikos.installment.domain.exception.AllocationExceedsInstallmentDueException;
import com.architek.oikos.installment.domain.exception.InsufficientAvailableCreditException;
import com.architek.oikos.installment.domain.exception.MovementNotCreditException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class AllocatePaymentServiceTest {

    @Mock
    private MovementLookupPort movementLookupPort;

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private AllocationRepository allocationRepository;

    private AllocatePaymentService newService() {
        return new AllocatePaymentService(movementLookupPort, installmentRepository, allocationRepository);
    }

    private static Installment installment(EntityId accountId, String amount) {
        return Installment.create(InstallmentId.newId(), accountId, EntityId.newId(), LocalDate.of(2027, 1, 1),
                Amount.of(new BigDecimal(amount)));
    }

    @Test
    void allocating_within_available_credit_and_remaining_due_succeeds() {
        EntityId accountId = EntityId.newId();
        EntityId movementId = EntityId.newId();
        Installment installment = installment(accountId, "250");
        when(movementLookupPort.findMovement(movementId))
                .thenReturn(Optional.of(new MovementInfo(accountId, true, new BigDecimal("500"))));
        when(installmentRepository.findById(installment.getId())).thenReturn(Optional.of(installment));
        when(allocationRepository.sumAllocatedByMovementId(movementId)).thenReturn(BigDecimal.ZERO);
        when(allocationRepository.sumAllocatedByInstallmentId(installment.getId())).thenReturn(BigDecimal.ZERO);
        when(allocationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().allocate(new AllocatePaymentCommand(movementId, installment.getId(), new BigDecimal("250")));
    }

    @Test
    void allocating_a_debit_movement_is_rejected() {
        EntityId accountId = EntityId.newId();
        EntityId movementId = EntityId.newId();
        Installment installment = installment(accountId, "250");
        when(movementLookupPort.findMovement(movementId))
                .thenReturn(Optional.of(new MovementInfo(accountId, false, new BigDecimal("500"))));

        assertThatThrownBy(() -> newService().allocate(
                new AllocatePaymentCommand(movementId, installment.getId(), BigDecimal.TEN)))
                .isInstanceOf(MovementNotCreditException.class);
    }

    @Test
    void allocating_across_different_accounts_is_rejected() {
        EntityId movementId = EntityId.newId();
        Installment installment = installment(EntityId.newId(), "250");
        when(movementLookupPort.findMovement(movementId))
                .thenReturn(Optional.of(new MovementInfo(EntityId.newId(), true, new BigDecimal("500"))));
        when(installmentRepository.findById(installment.getId())).thenReturn(Optional.of(installment));

        assertThatThrownBy(() -> newService().allocate(
                new AllocatePaymentCommand(movementId, installment.getId(), BigDecimal.TEN)))
                .isInstanceOf(AccountMismatchException.class);
    }

    @Test
    void allocating_more_than_the_available_credit_is_rejected() {
        EntityId accountId = EntityId.newId();
        EntityId movementId = EntityId.newId();
        Installment installment = installment(accountId, "250");
        when(movementLookupPort.findMovement(movementId))
                .thenReturn(Optional.of(new MovementInfo(accountId, true, new BigDecimal("100"))));
        when(installmentRepository.findById(installment.getId())).thenReturn(Optional.of(installment));
        when(allocationRepository.sumAllocatedByMovementId(movementId)).thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> newService().allocate(
                new AllocatePaymentCommand(movementId, installment.getId(), new BigDecimal("150"))))
                .isInstanceOf(InsufficientAvailableCreditException.class);
    }

    @Test
    void allocating_more_than_the_remaining_due_is_rejected() {
        EntityId accountId = EntityId.newId();
        EntityId movementId = EntityId.newId();
        Installment installment = installment(accountId, "250");
        when(movementLookupPort.findMovement(movementId))
                .thenReturn(Optional.of(new MovementInfo(accountId, true, new BigDecimal("500"))));
        when(installmentRepository.findById(installment.getId())).thenReturn(Optional.of(installment));
        when(allocationRepository.sumAllocatedByMovementId(movementId)).thenReturn(BigDecimal.ZERO);
        when(allocationRepository.sumAllocatedByInstallmentId(installment.getId())).thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> newService().allocate(
                new AllocatePaymentCommand(movementId, installment.getId(), new BigDecimal("300"))))
                .isInstanceOf(AllocationExceedsInstallmentDueException.class);
    }
}
