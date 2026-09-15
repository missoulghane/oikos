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
     * <p>Sans effet, et sans erreur, si ce contact détient déjà le lot : c'est
     * le cas nominal d'une invitation privée, où le syndic rattache le lot au
     * contact avant de l'inviter pour ce lot-là.
     *
     * @throws UnitUnavailableException if the unit belongs to someone else
     */
    void claim(EntityId unitId, EntityId partyId);
}
