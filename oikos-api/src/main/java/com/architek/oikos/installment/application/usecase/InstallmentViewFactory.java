package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.model.InstallmentStatusCalculator;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;

/**
 * Shared by every read use case that returns an InstallmentView, so the
 * amountPaid/remainingDue/status computation (RG011) is defined once.
 */
final class InstallmentViewFactory {

    private InstallmentViewFactory() {
    }

    static InstallmentView build(Installment installment, AllocationRepository allocationRepository, LocalDate today) {
        BigDecimal amountPaid = allocationRepository.sumAllocatedByInstallmentId(installment.getId());
        BigDecimal remainingDue = installment.getAmount().value().subtract(amountPaid);
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, amountPaid, today);

        return new InstallmentView(installment.getId(), installment.getAccountId(), installment.getUnitId(),
                installment.getDueDate(), installment.getAmount().value(), amountPaid, remainingDue, status);
    }
}
