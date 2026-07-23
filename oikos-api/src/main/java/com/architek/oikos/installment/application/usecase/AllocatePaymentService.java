package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.AllocatePaymentCommand;
import com.architek.oikos.installment.application.port.in.AllocatePaymentUseCase;
import com.architek.oikos.installment.application.port.out.MovementInfo;
import com.architek.oikos.installment.application.port.out.MovementLookupPort;
import com.architek.oikos.installment.domain.exception.AccountMismatchException;
import com.architek.oikos.installment.domain.exception.AllocationExceedsInstallmentDueException;
import com.architek.oikos.installment.domain.exception.InstallmentNotFoundException;
import com.architek.oikos.installment.domain.exception.InsufficientAvailableCreditException;
import com.architek.oikos.installment.domain.exception.MovementNotCreditException;
import com.architek.oikos.installment.domain.exception.MovementNotFoundException;
import com.architek.oikos.installment.domain.model.Allocation;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.AllocationId;

/**
 * Manual allocation (lettrage manuel). Enforces the same invariants the FIFO
 * engine relies on implicitly: only a credit movement can be allocated, both
 * sides must belong to the same account, and the amount cannot exceed either
 * the movement's remaining available credit or the installment's remaining
 * due. Movement details are validated through MovementLookupPort, never
 * accounting's repository directly (rule 4).
 */
@Component
public class AllocatePaymentService implements AllocatePaymentUseCase {

    private final MovementLookupPort movementLookupPort;
    private final InstallmentRepository installmentRepository;
    private final AllocationRepository allocationRepository;

    public AllocatePaymentService(MovementLookupPort movementLookupPort, InstallmentRepository installmentRepository,
                                   AllocationRepository allocationRepository) {
        this.movementLookupPort = movementLookupPort;
        this.installmentRepository = installmentRepository;
        this.allocationRepository = allocationRepository;
    }

    @Override
    @Transactional
    public AllocationId allocate(AllocatePaymentCommand command) {
        MovementInfo movement = movementLookupPort.findMovement(command.movementId())
                .orElseThrow(() -> new MovementNotFoundException(command.movementId()));
        if (!movement.credit()) {
            throw new MovementNotCreditException(command.movementId());
        }

        Installment installment = installmentRepository.findById(command.installmentId())
                .orElseThrow(() -> new InstallmentNotFoundException(command.installmentId()));

        if (!movement.accountId().equals(installment.getAccountId())) {
            throw new AccountMismatchException();
        }

        BigDecimal available = movement.amount()
                .subtract(allocationRepository.sumAllocatedByMovementId(command.movementId()));
        if (command.amount().compareTo(available) > 0) {
            throw new InsufficientAvailableCreditException(command.movementId());
        }

        BigDecimal due = installment.getAmount().value()
                .subtract(allocationRepository.sumAllocatedByInstallmentId(installment.getId()));
        if (command.amount().compareTo(due) > 0) {
            throw new AllocationExceedsInstallmentDueException(installment.getId());
        }

        Allocation allocation = Allocation.create(AllocationId.newId(), command.movementId(), installment.getId(),
                Amount.of(command.amount()));
        return allocationRepository.save(allocation).getId();
    }
}
