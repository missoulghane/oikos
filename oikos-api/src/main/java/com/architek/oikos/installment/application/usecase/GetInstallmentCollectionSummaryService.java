package com.architek.oikos.installment.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.port.in.GetInstallmentCollectionSummaryUseCase;
import com.architek.oikos.installment.application.port.out.PropertyUnitDirectoryPort;
import com.architek.oikos.installment.application.query.GetInstallmentCollectionSummaryQuery;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentCollectionSummary;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The same two steps as the listing beside it: resolve the property's units
 * through the property module's port, then ask the installments. Sharing that
 * shape is what makes the badge and the list it opens agree on which lots they
 * are talking about.
 */
@Component
public class GetInstallmentCollectionSummaryService implements GetInstallmentCollectionSummaryUseCase {

    private final PropertyUnitDirectoryPort propertyUnitDirectoryPort;
    private final InstallmentRepository installmentRepository;

    public GetInstallmentCollectionSummaryService(PropertyUnitDirectoryPort propertyUnitDirectoryPort,
                                                    InstallmentRepository installmentRepository) {
        this.propertyUnitDirectoryPort = propertyUnitDirectoryPort;
        this.installmentRepository = installmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InstallmentCollectionSummary getSummary(GetInstallmentCollectionSummaryQuery query) {
        List<EntityId> unitIds = propertyUnitDirectoryPort.listUnitIds(query.propertyId());
        return installmentRepository.summariseCollectible(unitIds, query.asOf());
    }
}
