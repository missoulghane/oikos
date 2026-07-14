package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Associe un contact (personne physique) a une fonction de gestion sur une
 * property. Le contact est reference par le type generique {@link EntityId}
 * pour garder property decouple du type ContactId propre a la feature
 * contact. Immutable: toute mutation retourne une nouvelle instance.
 * Semantique d'entite: equals/hashCode se basent sur l'identite (id), pas sur
 * les valeurs.
 */
public final class BoardMember {

    private final BoardMemberId id;
    private final PropertyId propertyId;
    private final EntityId contactId;
    private final BoardRole boardRole;

    private BoardMember(BoardMemberId id, PropertyId propertyId, EntityId contactId, BoardRole boardRole) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.contactId = Objects.requireNonNull(contactId, "contactId must not be null");
        this.boardRole = Objects.requireNonNull(boardRole, "boardRole must not be null");
    }

    public static BoardMember create(BoardMemberId id, PropertyId propertyId, EntityId contactId, BoardRole boardRole) {
        return new BoardMember(id, propertyId, contactId, boardRole);
    }

    public static BoardMember reconstruct(BoardMemberId id, PropertyId propertyId, EntityId contactId,
                                            BoardRole boardRole) {
        return new BoardMember(id, propertyId, contactId, boardRole);
    }

    public BoardMemberId getId() {
        return id;
    }

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public EntityId getContactId() {
        return contactId;
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
