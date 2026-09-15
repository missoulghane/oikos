package com.architek.oikos.property.application.usecase;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.application.command.ClaimUnitOwnershipCommand;
import com.architek.oikos.property.application.port.in.AddUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.in.ClaimUnitOwnershipUseCase;
import com.architek.oikos.property.domain.exception.OwnershipShareExceededException;
import com.architek.oikos.property.domain.exception.PartyAlreadyOwnsUnitException;
import com.architek.oikos.property.domain.exception.UnitAlreadyClaimedException;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;

/**
 * Revendiquer un lot, c'est en demander 100 % - part qui échoue
 * (OwnershipShareExceededException) dès qu'un propriétaire existe, si petite
 * que soit sa part : « le lot doit être entièrement libre » découle de
 * l'invariant existant, sans règle supplémentaire.
 *
 * <p>Un lot déjà détenu par ce contact-là n'est pas un échec : il n'y a rien à
 * lui attribuer, l'attribution est faite. C'est le cas nominal d'une invitation
 * privée, où le syndic rattache le lot au contact avant de l'inviter, et il est
 * tranché ici plutôt que rattrapé par l'appelant : l'exception d'un
 * AddUnitOwnershipService transactionnel marque la transaction rollback-only en
 * franchissant son proxy, et aucun catch en amont ne peut plus l'en défaire -
 * le commit final échouait alors en UnexpectedRollbackException, très loin de
 * la cause (voir AcceptMembershipRequestService).
 *
 * <p>Le verrou est pris ici, avant la lecture, et non plus laissé au seul
 * AddUnitOwnershipService : sans lui, deux revendications concurrentes
 * liraient toutes deux l'état d'avant.
 */
@Component
public class ClaimUnitOwnershipService implements ClaimUnitOwnershipUseCase {

    private static final BigDecimal FULL_SHARE = new BigDecimal("100");

    private final AddUnitOwnershipUseCase addUnitOwnershipUseCase;
    private final UnitRepository unitRepository;
    private final UnitOwnershipRepository unitOwnershipRepository;

    public ClaimUnitOwnershipService(AddUnitOwnershipUseCase addUnitOwnershipUseCase, UnitRepository unitRepository,
                                      UnitOwnershipRepository unitOwnershipRepository) {
        this.addUnitOwnershipUseCase = addUnitOwnershipUseCase;
        this.unitRepository = unitRepository;
        this.unitOwnershipRepository = unitOwnershipRepository;
    }

    @Override
    @Transactional
    public UnitOwnershipId claim(ClaimUnitOwnershipCommand command) {
        unitRepository.findByIdForUpdate(command.unitId())
                .orElseThrow(() -> new UnitNotFoundException(command.unitId()));

        Optional<UnitOwnership> alreadyOwned = unitOwnershipRepository.findAllByUnitId(command.unitId()).stream()
                .filter(ownership -> ownership.getPartyId().equals(command.partyId()))
                .findFirst();
        if (alreadyOwned.isPresent()) {
            return alreadyOwned.get().getId();
        }

        try {
            return addUnitOwnershipUseCase.add(new AddUnitOwnershipCommand(command.unitId(), command.partyId(), FULL_SHARE));
        } catch (OwnershipShareExceededException | PartyAlreadyOwnsUnitException e) {
            // Le lot appartient à quelqu'un d'autre. PartyAlreadyOwnsUnit ne peut
            // plus venir jusqu'ici (la lecture sous verrou ci-dessus l'a écarté) :
            // il reste attrapé pour que la traduction tienne si ce n'était plus vrai.
            throw new UnitAlreadyClaimedException(command.unitId());
        }
    }
}
