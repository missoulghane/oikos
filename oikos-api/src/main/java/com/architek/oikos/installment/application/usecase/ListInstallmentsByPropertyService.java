package com.architek.oikos.installment.application.usecase;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.port.in.ListInstallmentsByPropertyUseCase;
import com.architek.oikos.installment.application.port.out.PropertyUnitDirectoryPort;
import com.architek.oikos.installment.application.query.ListInstallmentsByPropertyQuery;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class ListInstallmentsByPropertyService implements ListInstallmentsByPropertyUseCase {

    private final PropertyUnitDirectoryPort propertyUnitDirectoryPort;
    private final InstallmentRepository installmentRepository;
    private final InstallmentCallRepository installmentCallRepository;

    public ListInstallmentsByPropertyService(PropertyUnitDirectoryPort propertyUnitDirectoryPort,
                                              InstallmentRepository installmentRepository,
                                              InstallmentCallRepository installmentCallRepository) {
        this.propertyUnitDirectoryPort = propertyUnitDirectoryPort;
        this.installmentRepository = installmentRepository;
        this.installmentCallRepository = installmentCallRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InstallmentView> listInstallments(ListInstallmentsByPropertyQuery query) {
        List<EntityId> unitIds = propertyUnitDirectoryPort.listUnitIds(query.propertyId(), query.search());
        if (unitIds.isEmpty()) {
            return Page.of(List.of(), query.pageRequest().pageNumber(), query.pageRequest().pageSize(), 0);
        }

        Page<Installment> page = installmentRepository.findPageByUnitIds(unitIds, query.filter(), query.pageRequest());
        Map<InstallmentCallId, YearMonth> periodByCallId = loadPeriodsByCallId(page.content());

        return page.map(installment -> InstallmentViewFactory.build(installment,
                installment.getInstallmentCallId() != null ? periodByCallId.get(installment.getInstallmentCallId()) : null));
    }

    private Map<InstallmentCallId, YearMonth> loadPeriodsByCallId(List<Installment> installments) {
        List<InstallmentCallId> callIds = installments.stream()
                .map(Installment::getInstallmentCallId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (callIds.isEmpty()) {
            return Map.of();
        }
        return installmentCallRepository.findAllByIds(callIds).stream()
                .collect(Collectors.toMap(InstallmentCall::getId, InstallmentCall::getPeriod));
    }
}
