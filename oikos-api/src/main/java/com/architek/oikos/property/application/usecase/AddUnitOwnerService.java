package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddUnitOwnerCommand;
import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnerUseCase;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Resolves or creates the Party behind an owner's email (get-or-create by
 * email, so re-using an existing party's address never fails), then delegates
 * to AddUnitOwnershipUseCase for the actual attachment - reuses its business
 * rules (party not already an owner, total share <= 100%) rather than
 * duplicating them. The unit is checked first so an invalid unitId never
 * leaves behind a Party created for nothing.
 */
@Component
public class AddUnitOwnerService implements AddUnitOwnerUseCase {

    private final UnitRepository unitRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final AddUnitOwnershipUseCase addUnitOwnershipUseCase;

    public AddUnitOwnerService(UnitRepository unitRepository, PartyDirectoryPort partyDirectoryPort,
                                AddUnitOwnershipUseCase addUnitOwnershipUseCase) {
        this.unitRepository = unitRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.addUnitOwnershipUseCase = addUnitOwnershipUseCase;
    }

    @Override
    @Transactional
    public UnitOwnershipId add(AddUnitOwnerCommand command) {
        unitRepository.findById(command.unitId()).orElseThrow(() -> new UnitNotFoundException(command.unitId()));

        EntityId partyId = partyDirectoryPort.findIdByEmail(command.email())
                .orElseGet(() -> partyDirectoryPort.createParty(
                        new PartyDetails(command.fullName(), command.partyType(), command.email())));

        return addUnitOwnershipUseCase.add(
                new AddUnitOwnershipCommand(command.unitId(), partyId, command.ownershipShare()));
    }
}
