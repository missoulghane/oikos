package com.architek.oikos.installment.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.port.in.ListInstallmentsByPropertyUseCase;
import com.architek.oikos.installment.application.port.out.PropertyUnitDirectoryPort;
import com.architek.oikos.installment.application.query.ListInstallmentsByPropertyQuery;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class ListInstallmentsByPropertyService implements ListInstallmentsByPropertyUseCase {

    private final PropertyUnitDirectoryPort propertyUnitDirectoryPort;
    private final InstallmentRepository installmentRepository;
    private final AllocationRepository allocationRepository;
    private final Clock clock;

    public ListInstallmentsByPropertyService(PropertyUnitDirectoryPort propertyUnitDirectoryPort,
                                              InstallmentRepository installmentRepository,
                                              AllocationRepository allocationRepository, Clock clock) {
        this.propertyUnitDirectoryPort = propertyUnitDirectoryPort;
        this.installmentRepository = installmentRepository;
        this.allocationRepository = allocationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InstallmentView> listInstallments(ListInstallmentsByPropertyQuery query) {
        List<EntityId> unitIds = propertyUnitDirectoryPort.listUnitIds(query.propertyId());
        if (unitIds.isEmpty()) {
            return Page.of(List.of(), query.pageRequest().pageNumber(), query.pageRequest().pageSize(), 0);
        }

        LocalDate today = LocalDate.now(clock);
        return installmentRepository.findPageByUnitIds(unitIds, query.filter(), today, query.pageRequest())
                .map(installment -> InstallmentViewFactory.build(installment, allocationRepository, today));
    }
}
