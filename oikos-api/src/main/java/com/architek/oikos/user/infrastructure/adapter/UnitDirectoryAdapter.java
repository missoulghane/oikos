package com.architek.oikos.user.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.application.dto.PartyLotView;
import com.architek.oikos.property.application.port.in.ListLotsByPartyUseCase;
import com.architek.oikos.property.application.query.ListLotsByPartyQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.out.OwnedUnitView;
import com.architek.oikos.user.application.port.out.UnitDirectoryPort;

@Component
public class UnitDirectoryAdapter implements UnitDirectoryPort {

    private final ListLotsByPartyUseCase listLotsByPartyUseCase;

    public UnitDirectoryAdapter(ListLotsByPartyUseCase listLotsByPartyUseCase) {
        this.listLotsByPartyUseCase = listLotsByPartyUseCase;
    }

    @Override
    public List<OwnedUnitView> listUnitsOwnedByParty(EntityId partyId) {
        return listLotsByPartyUseCase.listLots(new ListLotsByPartyQuery(partyId)).stream()
                .map(UnitDirectoryAdapter::toOwnedUnitView)
                .toList();
    }

    private static OwnedUnitView toOwnedUnitView(PartyLotView lot) {
        return new OwnedUnitView(EntityId.of(lot.unitId().toString()), lot.unitNumber(),
                EntityId.of(lot.buildingId().toString()), lot.buildingName(),
                EntityId.of(lot.propertyId().toString()), lot.propertyName(), lot.ownershipShare());
    }
}
