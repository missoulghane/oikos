package com.architek.oikos.property.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.PartyLotView;
import com.architek.oikos.property.application.port.in.ListLotsByPartyUseCase;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.application.query.ListLotsByPartyQuery;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;

/**
 * Lists every lot a party owns (via UnitOwnership), for the party's own
 * "fiche" page. Party existence is validated through PartyDirectoryPort (its
 * own not-found exception is left to bubble as-is, since property already
 * only depends on party's port-in/dto - never on its domain model or
 * exceptions directly, rule 6), the same way ListContactsByPropertyService
 * validates the property side of the same relationship.
 */
@Component
public class ListLotsByPartyService implements ListLotsByPartyUseCase {

    private final PartyDirectoryPort partyDirectoryPort;
    private final UnitOwnershipRepository unitOwnershipRepository;
    private final UnitRepository unitRepository;
    private final BuildingRepository buildingRepository;
    private final PropertyRepository propertyRepository;

    public ListLotsByPartyService(PartyDirectoryPort partyDirectoryPort, UnitOwnershipRepository unitOwnershipRepository,
                                   UnitRepository unitRepository, BuildingRepository buildingRepository,
                                   PropertyRepository propertyRepository) {
        this.partyDirectoryPort = partyDirectoryPort;
        this.unitOwnershipRepository = unitOwnershipRepository;
        this.unitRepository = unitRepository;
        this.buildingRepository = buildingRepository;
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyLotView> listLots(ListLotsByPartyQuery query) {
        partyDirectoryPort.getPartyById(query.partyId());

        return unitOwnershipRepository.findAllByPartyId(query.partyId()).stream()
                .map(this::toView)
                .toList();
    }

    private PartyLotView toView(UnitOwnership unitOwnership) {
        Unit unit = unitRepository.findById(unitOwnership.getUnitId())
                .orElseThrow(() -> new UnitNotFoundException(unitOwnership.getUnitId()));
        Building building = buildingRepository.findById(unit.getBuildingId())
                .orElseThrow(() -> new BuildingNotFoundException(unit.getBuildingId()));
        Property property = propertyRepository.findById(building.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException(building.getPropertyId()));

        return PartyLotView.from(unitOwnership, unit.getUnitNumber(), building.getId(), building.getName(),
                property.getId(), property.getName());
    }
}
