package com.architek.oikos.installment.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.port.in.ListInstallmentsByUnitUseCase;
import com.architek.oikos.installment.application.query.ListInstallmentsByUnitQuery;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;

@Component
public class ListInstallmentsByUnitService implements ListInstallmentsByUnitUseCase {

    private final InstallmentRepository installmentRepository;

    public ListInstallmentsByUnitService(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstallmentView> listInstallments(ListInstallmentsByUnitQuery query) {
        return installmentRepository.findAllByUnitId(query.unitId()).stream()
                .map(InstallmentViewFactory::build)
                .toList();
    }
}
