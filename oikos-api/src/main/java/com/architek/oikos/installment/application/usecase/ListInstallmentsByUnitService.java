package com.architek.oikos.installment.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.port.in.ListInstallmentsByUnitUseCase;
import com.architek.oikos.installment.application.query.ListInstallmentsByUnitQuery;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;

@Component
public class ListInstallmentsByUnitService implements ListInstallmentsByUnitUseCase {

    private final InstallmentRepository installmentRepository;
    private final AllocationRepository allocationRepository;
    private final Clock clock;

    public ListInstallmentsByUnitService(InstallmentRepository installmentRepository,
                                          AllocationRepository allocationRepository, Clock clock) {
        this.installmentRepository = installmentRepository;
        this.allocationRepository = allocationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstallmentView> listInstallments(ListInstallmentsByUnitQuery query) {
        LocalDate today = LocalDate.now(clock);
        return installmentRepository.findAllByUnitId(query.unitId()).stream()
                .map(installment -> InstallmentViewFactory.build(installment, allocationRepository, today))
                .toList();
    }
}
