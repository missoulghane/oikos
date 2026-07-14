package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Associe un contact (personne physique) a la possession d'un unit, avec sa
 * part de propriete. Le contact est reference par le type generique
 * {@link EntityId} pour garder property decouple du type ContactId propre
 * a la feature contact (meme raison que user.domain.model.User#contactId).
 * Immutable: toute mutation retourne une nouvelle instance. Semantique
 * d'entite: equals/hashCode se basent sur l'identite (id), pas sur les valeurs.
 */
public final class UnitOwnership {

    private final UnitOwnershipId id;
    private final UnitId unitId;
    private final EntityId contactId;
    private final OwnershipShare ownershipShare;

    private UnitOwnership(UnitOwnershipId id, UnitId unitId, EntityId contactId, OwnershipShare ownershipShare) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.contactId = Objects.requireNonNull(contactId, "contactId must not be null");
        this.ownershipShare = Objects.requireNonNull(ownershipShare, "ownershipShare must not be null");
    }

    public static UnitOwnership create(UnitOwnershipId id, UnitId unitId, EntityId contactId, OwnershipShare ownershipShare) {
        return new UnitOwnership(id, unitId, contactId, ownershipShare);
    }

    public static UnitOwnership reconstruct(UnitOwnershipId id, UnitId unitId, EntityId contactId, OwnershipShare ownershipShare) {
        return new UnitOwnership(id, unitId, contactId, ownershipShare);
    }

    public UnitOwnershipId getId() {
        return id;
    }

    public UnitId getUnitId() {
        return unitId;
    }

    public EntityId getContactId() {
        return contactId;
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
