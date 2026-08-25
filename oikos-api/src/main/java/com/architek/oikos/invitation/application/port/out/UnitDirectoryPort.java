package com.architek.oikos.invitation.application.port.out;

import java.util.Optional;

import com.architek.oikos.invitation.domain.exception.UnitUnavailableException;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to read unit identity/availability and to claim one on
 * an invitation's behalf. Implemented in invitation.infrastructure.adapter
 * by delegating to property's GetUnitUseCase/ClaimUnitOwnershipUseCase -
 * never to property's repository directly (rule 6).
 */
public interface UnitDirectoryPort {

    Optional<UnitBasicInfo> findBasicInfo(EntityId unitId);

    Page<AvailableUnitInfo> listAvailable(EntityId propertyId, PageRequest pageRequest);

    /**
     * Attaches partyId as the sole owner of unitId, atomically (see
     * property.ClaimUnitOwnershipUseCase's pessimistic lock).
     *
     * @throws UnitUnavailableException if the unit already has an owner
     */
    void claim(EntityId unitId, EntityId partyId);

    /**
     * Ce contact est-il déjà propriétaire de ce lot ? C'est le cas nominal
     * d'une invitation privée : le syndic rattache le lot au contact, puis
     * l'invite pour ce lot-là. Réserver le lot échouerait alors, alors qu'il
     * n'y a rien à réserver - seul l'accès reste à ouvrir.
     */
    boolean isOwnedBy(EntityId unitId, EntityId partyId);
}
