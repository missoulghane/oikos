package com.architek.oikos.installment.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.port.in.GetInstallmentUseCase;
import com.architek.oikos.installment.application.query.GetInstallmentQuery;
import com.architek.oikos.installment.domain.exception.InstallmentNotFoundException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;

@Component
public class GetInstallmentService implements GetInstallmentUseCase {

    private final InstallmentRepository installmentRepository;

    public GetInstallmentService(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InstallmentView getInstallment(GetInstallmentQuery query) {
        Installment installment = installmentRepository.findById(query.id())
                .orElseThrow(() -> new InstallmentNotFoundException(query.id()));
        return InstallmentViewFactory.build(installment);
    }
}
