package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.port.in.UpdateInstallmentSettlementUseCase;
import com.architek.oikos.installment.domain.exception.InstallmentNotFoundException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class UpdateInstallmentSettlementService implements UpdateInstallmentSettlementUseCase {

    private final InstallmentRepository installmentRepository;

    public UpdateInstallmentSettlementService(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    @Override
    @Transactional
    public void update(EntityId installmentId, BigDecimal outstandingAmount) {
        InstallmentId id = InstallmentId.of(installmentId.value());
        Installment installment = installmentRepository.findById(id)
                .orElseThrow(() -> new InstallmentNotFoundException(id));
        installmentRepository.save(installment.withOutstandingAmount(outstandingAmount));
    }
}
