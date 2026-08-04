package com.architek.oikos.invitation.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.out.AvailableUnitInfo;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.UnitUnavailableException;
import com.architek.oikos.property.application.command.ClaimUnitOwnershipCommand;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.ClaimUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListAvailableUnitsByPropertyUseCase;
import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.application.query.ListAvailableUnitsByPropertyQuery;
import com.architek.oikos.property.domain.exception.UnitAlreadyClaimedException;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to property's public port-in use cases
 * (GetUnitUseCase, ListAvailableUnitsByPropertyUseCase,
 * ClaimUnitOwnershipUseCase), never to property's repository directly (rule
 * 6). Translates property's UnitAlreadyClaimedException into invitation's
 * own UnitUnavailableException so invitation's application layer never
 * depends on property's exception types.
 */
@Component
public class InvitationUnitDirectoryAdapter implements UnitDirectoryPort {

    private final GetUnitUseCase getUnitUseCase;
    private final ListAvailableUnitsByPropertyUseCase listAvailableUnitsByPropertyUseCase;
    private final ClaimUnitOwnershipUseCase claimUnitOwnershipUseCase;

    public InvitationUnitDirectoryAdapter(GetUnitUseCase getUnitUseCase,
                                           ListAvailableUnitsByPropertyUseCase listAvailableUnitsByPropertyUseCase,
                                           ClaimUnitOwnershipUseCase claimUnitOwnershipUseCase) {
        this.getUnitUseCase = getUnitUseCase;
        this.listAvailableUnitsByPropertyUseCase = listAvailableUnitsByPropertyUseCase;
        this.claimUnitOwnershipUseCase = claimUnitOwnershipUseCase;
    }

    @Override
    public Optional<UnitBasicInfo> findBasicInfo(EntityId unitId) {
        try {
            UnitView view = getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(unitId.value())));
            boolean available = view.ownershipStatus() == OwnershipStatus.NOT_AFFECTED;
            EntityId propertyId = EntityId.of(view.propertyId().asUuid());
            return Optional.of(new UnitBasicInfo(propertyId, view.unitNumber(), view.unitTypeName(), available));
        } catch (UnitNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Page<AvailableUnitInfo> listAvailable(EntityId propertyId, PageRequest pageRequest) {
        Page<UnitView> page = listAvailableUnitsByPropertyUseCase.listAvailableUnits(
                new ListAvailableUnitsByPropertyQuery(new PropertyId(propertyId), pageRequest));
        return page.map(view -> new AvailableUnitInfo(EntityId.of(view.id().asUuid()), view.unitNumber(), view.unitTypeName()));
    }

    @Override
    public void claim(EntityId unitId, EntityId partyId) {
        try {
            claimUnitOwnershipUseCase.claim(new ClaimUnitOwnershipCommand(UnitId.of(unitId.value()), partyId));
        } catch (UnitAlreadyClaimedException e) {
            throw new UnitUnavailableException(e.getMessage());
        }
    }
}
