package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.dto.InstallmentAllocationView;
import com.architek.oikos.installment.application.dto.RegularizeUnitInstallmentsResult;
import com.architek.oikos.installment.application.port.in.RegularizeUnitInstallmentsUseCase;
import com.architek.oikos.installment.application.port.out.AdvanceRegularizationPort;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.UnitDirectoryPort;
import com.architek.oikos.installment.domain.exception.NothingToRegularizeException;
import com.architek.oikos.installment.domain.exception.PropertyNotFoundException;
import com.architek.oikos.installment.domain.exception.UnitNotFoundException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.model.PaymentAllocationCalculator;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Regularisation ("lettrage" - see the accounting-overview investigation
 * that motivated this feature): reuses the exact same FIFO split as an
 * owner payment (PaymentAllocationCalculator), except the "amount received"
 * is the unit's own already-recorded, still-unclaimed advance instead of a
 * fresh payment - so a fund call generated after an advance was booked can
 * still be settled from it, one click, without waiting for a new payment.
 * REQUIRES_NEW: RegularizePropertyInstallmentsService's sweep calls this
 * once per unit and catches NothingToRegularizeException to skip units with
 * nothing to do, but a plain @Transactional here would still mark the
 * *shared* transaction rollback-only the moment that exception is thrown
 * (Spring's default, regardless of whether the caller catches it) - the
 * sweep's own commit would then fail with UnexpectedRollbackException even
 * though every individual regularize() call "succeeded" from the caller's
 * point of view. Each unit's regularization must commit (or roll back)
 * independently, which is also the semantically correct behaviour for a
 * bulk sweep: one unit's failure must never undo another's.
 */
@Component
public class RegularizeUnitInstallmentsService implements RegularizeUnitInstallmentsUseCase {

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final InstallmentRepository installmentRepository;
    private final AdvanceRegularizationPort advanceRegularizationPort;

    public RegularizeUnitInstallmentsService(PropertyDirectoryPort propertyDirectoryPort,
                                              UnitDirectoryPort unitDirectoryPort,
                                              InstallmentRepository installmentRepository,
                                              AdvanceRegularizationPort advanceRegularizationPort) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.installmentRepository = installmentRepository;
        this.advanceRegularizationPort = advanceRegularizationPort;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RegularizeUnitInstallmentsResult regularize(RegularizeUnitInstallmentsCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }
        if (!unitDirectoryPort.exists(command.unitId())) {
            throw new UnitNotFoundException(command.unitId());
        }

        BigDecimal availableAdvance = advanceRegularizationPort.getAvailableAdvance(command.propertyId(),
                command.unitId());
        if (availableAdvance.signum() <= 0) {
            throw new NothingToRegularizeException(command.unitId());
        }

        List<Installment> unitInstallments = installmentRepository.findAllByUnitId(command.unitId());
        List<PaymentAllocationCalculator.UnsettledInstallment> unsettled = unitInstallments.stream()
                .filter(installment -> installment.getOutstandingAmount().signum() > 0)
                .map(installment -> new PaymentAllocationCalculator.UnsettledInstallment(
                        EntityId.of(installment.getId().asUuid()), installment.getDueDate(),
                        installment.getOutstandingAmount()))
                .toList();
        if (unsettled.isEmpty()) {
            throw new NothingToRegularizeException(command.unitId());
        }

        PaymentAllocationCalculator.Result allocationResult = PaymentAllocationCalculator.allocate(availableAdvance,
                unsettled);
        BigDecimal imputedTotal = allocationResult.allocations().stream()
                .map(PaymentAllocationCalculator.InstallmentAllocation::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (imputedTotal.signum() <= 0) {
            throw new NothingToRegularizeException(command.unitId());
        }

        EntityId journalEntryId = advanceRegularizationPort.postRegularizationEntry(command.propertyId(),
                command.unitId(), command.pieceDate(), imputedTotal, command.createdByUserId());

        for (PaymentAllocationCalculator.InstallmentAllocation allocation : allocationResult.allocations()) {
            Installment installment = unitInstallments.stream()
                    .filter(candidate -> candidate.getId().asUuid().equals(allocation.installmentId().value()))
                    .findFirst()
                    .orElseThrow();
            BigDecimal newOutstanding = installment.getOutstandingAmount().subtract(allocation.amount());
            installmentRepository.save(installment.withOutstandingAmount(newOutstanding));
        }

        List<InstallmentAllocationView> allocationViews = allocationResult.allocations().stream()
                .map(allocation -> new InstallmentAllocationView(allocation.installmentId(), allocation.amount()))
                .toList();
        return new RegularizeUnitInstallmentsResult(command.unitId(), journalEntryId, imputedTotal, allocationViews);
    }
}
