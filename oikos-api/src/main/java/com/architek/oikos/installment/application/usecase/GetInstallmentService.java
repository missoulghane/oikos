package com.architek.oikos.installment.application.usecase;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.port.in.GetInstallmentUseCase;
import com.architek.oikos.installment.application.query.GetInstallmentQuery;
import com.architek.oikos.installment.domain.exception.InstallmentNotFoundException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;

@Component
public class GetInstallmentService implements GetInstallmentUseCase {

    private final InstallmentRepository installmentRepository;
    private final AllocationRepository allocationRepository;
    private final Clock clock;

    public GetInstallmentService(InstallmentRepository installmentRepository, AllocationRepository allocationRepository,
                                  Clock clock) {
        this.installmentRepository = installmentRepository;
        this.allocationRepository = allocationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public InstallmentView getInstallment(GetInstallmentQuery query) {
        Installment installment = installmentRepository.findById(query.id())
                .orElseThrow(() -> new InstallmentNotFoundException(query.id()));
        return InstallmentViewFactory.build(installment, allocationRepository, LocalDate.now(clock));
    }
}
