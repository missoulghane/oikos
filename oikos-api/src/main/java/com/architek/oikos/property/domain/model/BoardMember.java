package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardMemberStatus;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Associe un party (personne physique) a une fonction de gestion sur une
 * property. Le party est reference par le type generique {@link EntityId}
 * pour garder property decouple du type PartyId propre a la feature
 * party. Immutable: toute mutation retourne une nouvelle instance.
 * Semantique d'entite: equals/hashCode se basent sur l'identite (id), pas sur
 * les valeurs.
 *
 * userId est nullable: renseigne uniquement quand ce BoardMember provient du
 * flux d'acceptation d'invitation (CreatePendingBoardMemberService), ou
 * l'AppUser qui accepte est connu et l'octroi de son role sur la property est
 * volontairement differe (voir status ci-dessous). Un membre ajoute
 * directement par un admin (AddBoardMemberService) n'a pas d'utilisateur
 * associe a la creation - seulement un party, eventuellement cree a la volee
 * - donc userId reste null dans ce cas.
 *
 * status indique si le role PROPERTY_BOARD_MEMBER a effectivement ete
 * accorde pour ce siege: ACTIVE signifie que oui (ajout direct par un admin,
 * ou siege en attente valide depuis); PENDING_VALIDATION signifie que le
 * siege existe (l'invitation a ete acceptee) mais qu'un admin doit encore le
 * valider explicitement avant que le role soit accorde.
 */
public final class BoardMember {

    private final BoardMemberId id;
    private final PropertyId propertyId;
    private final EntityId partyId;
    private final EntityId userId;
    private final BoardRole boardRole;
    private final BoardMemberStatus status;

    private BoardMember(BoardMemberId id, PropertyId propertyId, EntityId partyId, EntityId userId,
                         BoardRole boardRole, BoardMemberStatus status) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.partyId = Objects.requireNonNull(partyId, "partyId must not be null");
        this.userId = userId;
        this.boardRole = Objects.requireNonNull(boardRole, "boardRole must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public static BoardMember create(BoardMemberId id, PropertyId propertyId, EntityId partyId, BoardRole boardRole) {
        return new BoardMember(id, propertyId, partyId, null, boardRole, BoardMemberStatus.ACTIVE);
    }

    /**
     * Utilisee par CreatePendingBoardMemberService lors de l'acceptation
     * d'une invitation de bureau: le siege existe et est rattache au userId
     * qui accepte, mais demarre en PENDING_VALIDATION - aucun role n'est
     * accorde tant qu'un admin n'appelle pas {@link #activate()} via
     * ValidateBoardMemberService.
     */
    public static BoardMember createPending(BoardMemberId id, PropertyId propertyId, EntityId partyId,
                                              EntityId userId, BoardRole boardRole) {
        return new BoardMember(id, propertyId, partyId,
                Objects.requireNonNull(userId, "userId must not be null"), boardRole, BoardMemberStatus.PENDING_VALIDATION);
    }

    public static BoardMember reconstruct(BoardMemberId id, PropertyId propertyId, EntityId partyId, EntityId userId,
                                            BoardRole boardRole, BoardMemberStatus status) {
        return new BoardMember(id, propertyId, partyId, userId, boardRole, status);
    }

    /** Active le siege: n'a de sens que depuis PENDING_VALIDATION, verifie par ValidateBoardMemberService. */
    public BoardMember activate() {
        return new BoardMember(id, propertyId, partyId, userId, boardRole, BoardMemberStatus.ACTIVE);
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

    /** Nullable - renseigne uniquement pour les membres crees via le flux d'acceptation d'invitation. */
    public EntityId getUserId() {
        return userId;
    }

    public BoardRole getBoardRole() {
        return boardRole;
    }

    public BoardMemberStatus getStatus() {
        return status;
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
