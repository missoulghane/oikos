package com.architek.oikos.property.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.UnitOwnershipView;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.application.query.ListUnitOwnershipsByUnitQuery;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;

@Component
public class ListUnitOwnershipsByUnitService implements ListUnitOwnershipsByUnitUseCase {

    private final UnitOwnershipRepository unitOwnershipRepository;
    private final PartyDirectoryPort partyDirectoryPort;

    public ListUnitOwnershipsByUnitService(UnitOwnershipRepository unitOwnershipRepository,
                                            PartyDirectoryPort partyDirectoryPort) {
        this.unitOwnershipRepository = unitOwnershipRepository;
        this.partyDirectoryPort = partyDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitOwnershipView> listUnitOwnerships(ListUnitOwnershipsByUnitQuery query) {
        return unitOwnershipRepository.findAllByUnitId(query.unitId()).stream()
                .map(unitOwnership -> UnitOwnershipView.from(unitOwnership,
                        partyDirectoryPort.getPartyById(unitOwnership.getPartyId())))
                .toList();
    }
}
