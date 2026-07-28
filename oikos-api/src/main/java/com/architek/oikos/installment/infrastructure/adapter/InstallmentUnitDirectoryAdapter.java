package com.architek.oikos.installment.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.port.out.UnitDirectoryPort;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.query.GetUnitQuery;
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

    public InstallmentUnitDirectoryAdapter(GetUnitUseCase getUnitUseCase) {
        this.getUnitUseCase = getUnitUseCase;
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
