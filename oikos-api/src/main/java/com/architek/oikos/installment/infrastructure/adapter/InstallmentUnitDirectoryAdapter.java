package com.architek.oikos.installment.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.port.out.UnitDirectoryPort;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.application.query.ListUnitOwnershipsByUnitQuery;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to property's public port-in
 * (GetUnitUseCase), never to property's repository directly (rule 4). Named
 * distinctly from property's own PropertyPartyDirectoryAdapter to avoid a
 * Spring bean name collision between same-named classes in different
 * packages.
 */
@Component
public class InstallmentUnitDirectoryAdapter implements UnitDirectoryPort {

    private final GetUnitUseCase getUnitUseCase;
    private final ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase;

    public InstallmentUnitDirectoryAdapter(GetUnitUseCase getUnitUseCase,
                                            ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase) {
        this.getUnitUseCase = getUnitUseCase;
        this.listUnitOwnershipsByUnitUseCase = listUnitOwnershipsByUnitUseCase;
    }

    @Override
    public String getUnitNumber(EntityId unitId) {
        return getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(unitId.value()))).unitNumber();
    }

    /**
     * Through the ownerships rather than UnitView.ownerFullNames: GetUnitService
     * builds the view with the overload that leaves that list empty, so reading it
     * here would silently yield no owner at all.
     */
    @Override
    public java.util.List<String> getOwnerFullNames(EntityId unitId) {
        return listUnitOwnershipsByUnitUseCase
                .listUnitOwnerships(new ListUnitOwnershipsByUnitQuery(UnitId.of(unitId.value())))
                .stream()
                .map(com.architek.oikos.property.application.dto.UnitOwnershipView::partyFullName)
                .toList();
    }

    @Override
    public boolean exists(EntityId unitId) {
        try {
            getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(unitId.value())));
            return true;
        } catch (UnitNotFoundException e) {
            return false;
        }
    }
}
