package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Associe un party (personne physique) a la possession d'un unit, avec sa
 * part de propriete. Le party est reference par le type generique
 * {@link EntityId} pour garder property decouple du type PartyId propre
 * a la feature party (meme raison que user.domain.model.User#partyId).
 * Immutable: toute mutation retourne une nouvelle instance. Semantique
 * d'entite: equals/hashCode se basent sur l'identite (id), pas sur les valeurs.
 */
public final class UnitOwnership {

    private final UnitOwnershipId id;
    private final UnitId unitId;
    private final EntityId partyId;
    private final OwnershipShare ownershipShare;

    private UnitOwnership(UnitOwnershipId id, UnitId unitId, EntityId partyId, OwnershipShare ownershipShare) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.partyId = Objects.requireNonNull(partyId, "partyId must not be null");
        this.ownershipShare = Objects.requireNonNull(ownershipShare, "ownershipShare must not be null");
    }

    public static UnitOwnership create(UnitOwnershipId id, UnitId unitId, EntityId partyId, OwnershipShare ownershipShare) {
        return new UnitOwnership(id, unitId, partyId, ownershipShare);
    }

    public static UnitOwnership reconstruct(UnitOwnershipId id, UnitId unitId, EntityId partyId, OwnershipShare ownershipShare) {
        return new UnitOwnership(id, unitId, partyId, ownershipShare);
    }

    public UnitOwnershipId getId() {
        return id;
    }

    public UnitId getUnitId() {
        return unitId;
    }

    public EntityId getPartyId() {
        return partyId;
    }

    public OwnershipShare getOwnershipShare() {
        return ownershipShare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof UnitOwnership other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
