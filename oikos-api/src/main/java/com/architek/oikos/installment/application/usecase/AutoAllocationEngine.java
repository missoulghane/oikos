package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.port.out.AccountMovement;
import com.architek.oikos.installment.application.port.out.AccountMovementsPort;
import com.architek.oikos.installment.domain.model.Allocation;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * FIFO auto-allocation: matches an account's oldest unpaid/partially-paid
 * installments (by due date) against its oldest available credit movements
 * (by occurredOn), greedily, until one side runs out. Invoked after both
 * RecordPaymentService (a payment may settle existing due installments,
 * triggered via accounting's AutoAllocationPort) and RecordInstallmentCallService
 * (a new installment may be settled by an already-available credit - the
 * advance-payment scenario) so the match triggers symmetrically regardless of
 * which side was created last. Movement data is read through
 * AccountMovementsPort, never accounting's repository directly (rule 4).
 * Not exposed as its own port-in for that second caller: only the
 * cross-module trigger (AutoAllocateUseCase) is public; this class itself is
 * an internal collaborator (manual allocation goes through
 * AllocatePaymentUseCase instead).
 */
@Component
class AutoAllocationEngine {

    private final AccountMovementsPort accountMovementsPort;
    private final InstallmentRepository installmentRepository;
    private final AllocationRepository allocationRepository;

    AutoAllocationEngine(AccountMovementsPort accountMovementsPort, InstallmentRepository installmentRepository,
                          AllocationRepository allocationRepository) {
        this.accountMovementsPort = accountMovementsPort;
        this.installmentRepository = installmentRepository;
        this.allocationRepository = allocationRepository;
    }

    void allocate(EntityId accountId) {
        List<AccountMovement> credits = accountMovementsPort.listCreditMovements(accountId).stream()
                .sorted(Comparator.comparing(AccountMovement::occurredOn))
                .toList();
        List<Installment> installments = installmentRepository.findAllByAccountId(accountId).stream()
                .sorted(Comparator.comparing(Installment::getDueDate))
                .toList();

        if (credits.isEmpty() || installments.isEmpty()) {
            return;
        }

        Map<EntityId, BigDecimal> availableByMovement = new HashMap<>();
        for (AccountMovement movement : credits) {
            BigDecimal available = movement.amount()
                    .subtract(allocationRepository.sumAllocatedByMovementId(movement.id()));
            availableByMovement.put(movement.id(), available);
        }

        Map<InstallmentId, BigDecimal> dueByInstallment = new HashMap<>();
        for (Installment installment : installments) {
            BigDecimal due = installment.getAmount().value()
                    .subtract(allocationRepository.sumAllocatedByInstallmentId(installment.getId()));
            dueByInstallment.put(installment.getId(), due);
        }

        for (Installment installment : installments) {
            BigDecimal due = dueByInstallment.get(installment.getId());
            if (due.signum() <= 0) {
                continue;
            }
            for (AccountMovement movement : credits) {
                BigDecimal available = availableByMovement.get(movement.id());
                if (available.signum() <= 0) {
                    continue;
                }
                BigDecimal allocated = due.min(available);
                if (allocated.signum() > 0) {
                    allocationRepository.save(Allocation.create(AllocationId.newId(), movement.id(),
                            installment.getId(), Amount.of(allocated)));
                    due = due.subtract(allocated);
                    available = available.subtract(allocated);
                    dueByInstallment.put(installment.getId(), due);
                    availableByMovement.put(movement.id(), available);
                }
                if (due.signum() <= 0) {
                    break;
                }
            }
        }
    }
}
