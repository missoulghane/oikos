package com.architek.oikos.property.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddUnitOwnerCommand;
import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnerUseCase;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.out.AccountLinkingPort;
import com.architek.oikos.property.application.port.out.AccountDirectoryPort;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Resolves or creates the Party behind an owner, then delegates to
 * AddUnitOwnershipUseCase for the actual attachment - reuses its business rules
 * (party not already an owner, total share &lt;= 100%) rather than duplicating
 * them. The unit is checked first so an invalid unitId never leaves behind a
 * Party created for nothing.
 *
 * <p>Get-or-create looks up the email first, then the phone. Both are unique per
 * property, and a syndic often knows one without the other: matching on a single
 * one of them created a second fiche for a person already on file, which is paid
 * for later in duplicate convocations and dues calls.
 *
 * <p>The account-linking invitation is sent only when the caller asks for it
 * (see {@link AddUnitOwnerCommand#invite()}). It used to go out on every call,
 * which is right for an owner being onboarded and wrong for the syndic simply
 * recording who owns what - the recipient got an invitation nobody told them
 * about. It stays a no-op when the party already has an account.
 */
@Component
public class AddUnitOwnerService implements AddUnitOwnerUseCase {

    private final UnitRepository unitRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final AddUnitOwnershipUseCase addUnitOwnershipUseCase;
    private final AccountLinkingPort accountLinkingPort;
    private final AccountDirectoryPort accountDirectoryPort;

    public AddUnitOwnerService(UnitRepository unitRepository, PartyDirectoryPort partyDirectoryPort,
                                AddUnitOwnershipUseCase addUnitOwnershipUseCase, AccountLinkingPort accountLinkingPort,
                                AccountDirectoryPort accountDirectoryPort) {
        this.unitRepository = unitRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.addUnitOwnershipUseCase = addUnitOwnershipUseCase;
        this.accountLinkingPort = accountLinkingPort;
        this.accountDirectoryPort = accountDirectoryPort;
    }

    private Optional<EntityId> resolveByEmail(AddUnitOwnerCommand command, EntityId propertyId) {
        return command.email() == null ? Optional.empty() : partyDirectoryPort.findIdByEmail(command.email(), propertyId);
    }

    @Override
    @Transactional
    public UnitOwnershipId add(AddUnitOwnerCommand command) {
        Unit unit = unitRepository.findById(command.unitId()).orElseThrow(() -> new UnitNotFoundException(command.unitId()));
        EntityId propertyId = EntityId.of(unit.getPropertyId().asUuid());

        // Email de fiche, téléphone, puis email de compte : le syndic tape
        // souvent l'adresse avec laquelle la personne se connecte, qui n'est pas
        // celle inscrite sur sa fiche (voir AccountDirectoryPort). Sans ce
        // troisième filet, un second contact naît pour quelqu'un déjà au fichier.
        EntityId partyId = resolveByEmail(command, propertyId)
                .or(() -> partyDirectoryPort.findIdByPhone(command.phone(), propertyId))
                .or(() -> command.email() != null
                        ? accountDirectoryPort.findLinkedPartyInProperty(command.email(), propertyId)
                        : Optional.empty())
                .orElseGet(() -> partyDirectoryPort.createParty(
                        new PartyDetails(command.fullName(), command.partyType(), command.email(), command.phone()),
                        propertyId));

        // Pas d'adresse, pas d'invitation : le lien de création de compte part par
        // email et n'a nulle part où aller. Le lot est rattaché quand même.
        if (command.invite() && command.email() != null) {
            accountLinkingPort.inviteOwnerIfUnlinked(partyId, command.email(), command.fullName());
        }

        return addUnitOwnershipUseCase.add(
                new AddUnitOwnershipCommand(command.unitId(), partyId, command.ownershipShare()));
    }
}
