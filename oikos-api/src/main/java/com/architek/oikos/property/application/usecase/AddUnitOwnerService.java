package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddUnitOwnerCommand;
import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnerUseCase;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.out.AccountLinkingPort;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Resolves or creates the Party behind an owner's email (get-or-create by
 * email, so re-using an existing party's address never fails), then delegates
 * to AddUnitOwnershipUseCase for the actual attachment - reuses its business
 * rules (party not already an owner, total share <= 100%) rather than
 * duplicating them. The unit is checked first so an invalid unitId never
 * leaves behind a Party created for nothing. Once the party is resolved, an
 * account-linking invitation is sent (a no-op if that party is already
 * linked to an AppUser), so the owner can eventually see their own lots.
 */
@Component
public class AddUnitOwnerService implements AddUnitOwnerUseCase {

    private final UnitRepository unitRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final AddUnitOwnershipUseCase addUnitOwnershipUseCase;
    private final AccountLinkingPort accountLinkingPort;

    public AddUnitOwnerService(UnitRepository unitRepository, PartyDirectoryPort partyDirectoryPort,
                                AddUnitOwnershipUseCase addUnitOwnershipUseCase, AccountLinkingPort accountLinkingPort) {
        this.unitRepository = unitRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.addUnitOwnershipUseCase = addUnitOwnershipUseCase;
        this.accountLinkingPort = accountLinkingPort;
    }

    @Override
    @Transactional
    public UnitOwnershipId add(AddUnitOwnerCommand command) {
        Unit unit = unitRepository.findById(command.unitId()).orElseThrow(() -> new UnitNotFoundException(command.unitId()));
        EntityId propertyId = EntityId.of(unit.getPropertyId().asUuid());

        EntityId partyId = partyDirectoryPort.findIdByEmail(command.email(), propertyId)
                .orElseGet(() -> partyDirectoryPort.createParty(
                        new PartyDetails(command.fullName(), command.partyType(), command.email(), command.phone()),
                        propertyId));

        accountLinkingPort.inviteOwnerIfUnlinked(partyId, command.email(), command.fullName());

        return addUnitOwnershipUseCase.add(
                new AddUnitOwnershipCommand(command.unitId(), partyId, command.ownershipShare()));
    }
}
