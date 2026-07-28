package com.architek.oikos.installment.application.usecase;

import java.time.LocalDate;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.model.InstallmentStatusCalculator;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;

/**
 * Shared by every read use case that returns an InstallmentView, so the
 * status computation is defined once.
 */
final class InstallmentViewFactory {

    private InstallmentViewFactory() {
    }

    static InstallmentView build(Installment installment, LocalDate today) {
        InstallmentStatus status = InstallmentStatusCalculator.compute(installment, today);

        return new InstallmentView(installment.getId(), installment.getUnitId(),
                installment.getDueDate(), installment.getAmount().value(), status);
    }
}
