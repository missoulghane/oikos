package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Associe un party (personne physique) a une fonction de gestion sur une
 * property. Le party est reference par le type generique {@link EntityId}
 * pour garder property decouple du type PartyId propre a la feature
 * party. Immutable: toute mutation retourne une nouvelle instance.
 * Semantique d'entite: equals/hashCode se basent sur l'identite (id), pas sur
 * les valeurs.
 */
public final class BoardMember {

    private final BoardMemberId id;
    private final PropertyId propertyId;
    private final EntityId partyId;
    private final BoardRole boardRole;

    private BoardMember(BoardMemberId id, PropertyId propertyId, EntityId partyId, BoardRole boardRole) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.partyId = Objects.requireNonNull(partyId, "partyId must not be null");
        this.boardRole = Objects.requireNonNull(boardRole, "boardRole must not be null");
    }

    public static BoardMember create(BoardMemberId id, PropertyId propertyId, EntityId partyId, BoardRole boardRole) {
        return new BoardMember(id, propertyId, partyId, boardRole);
    }

    public static BoardMember reconstruct(BoardMemberId id, PropertyId propertyId, EntityId partyId,
                                            BoardRole boardRole) {
        return new BoardMember(id, propertyId, partyId, boardRole);
    }

    public BoardMemberId getId() {
        return id;
    }

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public EntityId getPartyId() {
        return partyId;
    }

    public BoardRole getBoardRole() {
        return boardRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof BoardMember other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
