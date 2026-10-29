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
 * fresh payment - so a fund call generated after an advance was booked is
 * settled from it. Four triggers: PaymentAdvanceRegularizationListener when
 * a recette is recorded on the lot, InstallmentCallAdvanceRegularizationListener
 * when a call charges it, and RegularizeAdvancesUseCase - the daily sweep and
 * the back-office's per-lot action.
 * Same due-date cutoff as a payment, on the piece date: this is the mechanism
 * that eventually imputes an advance onto a call left out at payment time, once
 * that call has actually fallen due.
 * REQUIRES_NEW: its listener callers run AFTER_COMMIT, when the triggering
 * transaction is already committed but still bound to the thread. A plain @Transactional here would join that finished
 * transaction and its writes would never be committed; this regularization
 * needs a transaction of its own. The sweep relies on it too: one lot per
 * transaction.
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
        if (!unitDirectoryPort.existsInProperty(command.unitId(), command.propertyId())) {
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

        // Same cutoff as an owner payment, on the piece date of the regularization:
        // an advance is precisely what has not been imputed yet, and consuming it
        // on a call not yet due would only move the problem forward.
        PaymentAllocationCalculator.Result allocationResult = PaymentAllocationCalculator.allocate(availableAdvance,
                unsettled, command.pieceDate());
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
