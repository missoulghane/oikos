package com.architek.oikos.messaging.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Une liste de destinataires nommée, propre à une copropriété : « Habitants du
 * bâtiment 1 », « Locataires », etc. Créée et entretenue par le bureau
 * (bénévole ou pro, voir managesProperty), elle sert à composer sans
 * re-sélectionner les mêmes personnes une par une.
 *
 * <p>Ce n'est qu'un carnet d'adresses : envoyer à un groupe crée une
 * conversation ordinaire avec ses membres comme participants (voir
 * StartGroupConversationService), donc chaque envoi vit sa vie et le groupe
 * n'apparaît nulle part dans le fil. Renommer ou vider un groupe plus tard ne
 * touche donc à aucun message déjà parti - c'est voulu : un message est adressé
 * aux gens, pas à une liste qui bouge.
 *
 * <p>Les membres sont des userId, comme partout ailleurs en messagerie : un
 * membre de la copropriété sans compte ne peut rien recevoir, il n'a donc rien
 * à faire dans un groupe (voir PartyAccountDirectoryPort.resolveUserIds).
 *
 * <p>Immuable : chaque modification rend une nouvelle instance. Identité par
 * l'id, comme Conversation.
 */
public final class RecipientGroup {

    private static final int MAX_NAME_LENGTH = 120;

    private final RecipientGroupId id;
    private final EntityId propertyId;
    private final String name;
    private final Set<EntityId> memberUserIds;
    private final EntityId createdBy;
    private final Instant createdDate;

    private RecipientGroup(RecipientGroupId id, EntityId propertyId, String name, Set<EntityId> memberUserIds,
                            EntityId createdBy, Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("a recipient group must have a name");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
        Set<EntityId> members = Set.copyOf(Objects.requireNonNull(memberUserIds, "memberUserIds must not be null"));
        if (members.isEmpty()) {
            throw new IllegalArgumentException("a recipient group must have at least one member");
        }
        this.name = name.trim();
        this.memberUserIds = members;
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy must not be null");
        this.createdDate = createdDate;
    }

    public static RecipientGroup create(RecipientGroupId id, EntityId propertyId, String name,
                                          Set<EntityId> memberUserIds, EntityId createdBy) {
        return new RecipientGroup(id, propertyId, name, memberUserIds, createdBy, null);
    }

    public static RecipientGroup reconstruct(RecipientGroupId id, EntityId propertyId, String name,
                                               Set<EntityId> memberUserIds, EntityId createdBy, Instant createdDate) {
        return new RecipientGroup(id, propertyId, name, memberUserIds, createdBy, createdDate);
    }

    /** Renomme et remplace les membres d'un coup : l'écran de gestion enregistre les deux ensemble. */
    public RecipientGroup update(String newName, Set<EntityId> newMemberUserIds) {
        return new RecipientGroup(id, propertyId, newName, newMemberUserIds, createdBy, createdDate);
    }

    public RecipientGroupId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public String getName() {
        return name;
    }

    public Set<EntityId> getMemberUserIds() {
        return memberUserIds;
    }

    public EntityId getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof RecipientGroup other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
