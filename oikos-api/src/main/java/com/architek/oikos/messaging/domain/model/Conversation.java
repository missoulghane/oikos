package com.architek.oikos.messaging.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A conversation belongs to exactly one property. GROUP conversations carry
 * 2..N applicatively-chosen participantUserIds (the sender plus one or more
 * recipients), stored as plain rows in the conversation_participant join
 * table (see V17__messaging.sql) - one row per (conversation, user), and a
 * required subject (email-style title, set once at compose time - see
 * ConversationSubject). Unlike the old DIRECT type this replaced, there is
 * no uniqueness constraint on the participant set: composing a new GROUP
 * conversation to the same set of people as an earlier one never reuses that
 * earlier conversation, it always creates a brand-new row (Outlook-style
 * "New message" semantics) - see StartGroupConversationService, which never
 * performs a find-or-create for this type. Resuming an existing conversation
 * instead happens by simply reopening it from the inbox list
 * (ListMyConversationsUseCase), unrelated to creation.
 *
 * <p>BOARD_PRIVATE conversations require a subject like GROUP (several
 * distinct threads can exist per property, e.g. one per topic the board is
 * arbitrating), but store no participants: membership is resolved
 * dynamically from the property's *current* staff roster
 * (UserAccessPort/PropertyMemberDirectoryPort), the same mechanism BROADCAST
 * uses - an ex-board-member loses access to the whole thread immediately,
 * a newly elected one sees its history, nothing to migrate either way. No
 * uniqueness constraint (unlike BROADCAST): starting a new board-private
 * thread never reuses an earlier one, matching GROUP's "New message"
 * semantics - see StartBoardConversationService.
 *
 * <p>BROADCAST conversations carry no stored participants but do carry a
 * subject, like the two types above: un envoi groupé n'est pas un fil, c'est
 * un message adressé à toute la copropriété, avec son propre objet, et le
 * suivant est un autre message - jamais une réponse au précédent. Il n'y a
 * donc plus de canal unique par copropriété (l'index unique
 * uk_conversation_broadcast_property est levé par V7) ni de find-or-create :
 * chaque envoi crée sa propre ligne, comme GROUP et BOARD_PRIVATE.
 * Membership is resolved dynamically from the property's current roster at
 * read time, same as BOARD_PRIVATE above.
 *
 * <p>Le sujet reste nullable pour ce seul type, et uniquement pour les lignes
 * antérieures à ce changement : elles ont été écrites sans objet, et rien ne
 * permet de leur en inventer un. createBroadcast, lui, en exige un.
 *
 * <p>createdDate mirrors the persistence layer's audited created_date
 * (read-only from the domain's point of view - AuditableEntity/JPA auditing
 * is the sole writer): null on a freshly created, not-yet-persisted instance,
 * populated by the mapper once reloaded from a saved entity.
 *
 * <p>concernsUnit is an optional free-text lot label (e.g. "Appartement 3"),
 * set once at compose time, GROUP only: a hint disambiguating which of a
 * multi-lot recipient's units the thread is about (see NewConversationPage's
 * "Concerne" picker, sourced from that recipient's own
 * RecipientCandidate.unitNumbers - no foreign key, nothing validated against
 * the property's actual unit registry, this is a display hint, not a
 * structural link).
 *
 * <p>Immutable: this aggregate never changes after creation (nothing to
 * mutate - unlike Invitation/BoardMember, there is no lifecycle here beyond
 * "exists"). Entity semantics: equals/hashCode are identity-based.
 */
public final class Conversation {

    private final ConversationId id;
    private final EntityId propertyId;
    private final ConversationType type;
    private final EntityId createdBy;
    private final Set<EntityId> participantUserIds;
    private final ConversationSubject subject;
    private final String concernsUnit;
    private final Instant createdDate;

    private Conversation(ConversationId id, EntityId propertyId, ConversationType type, EntityId createdBy,
                          Set<EntityId> participantUserIds, ConversationSubject subject, String concernsUnit, Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy must not be null");
        Set<EntityId> participants = Set.copyOf(Objects.requireNonNull(participantUserIds, "participantUserIds must not be null"));
        if (type == ConversationType.GROUP && participants.size() < 2) {
            throw new IllegalArgumentException("a GROUP conversation must have at least 2 participants");
        }
        if (type != ConversationType.GROUP && !participants.isEmpty()) {
            throw new IllegalArgumentException("a " + type + " conversation must not store participants");
        }
        if ((type == ConversationType.GROUP || type == ConversationType.BOARD_PRIVATE) && subject == null) {
            throw new IllegalArgumentException("a " + type + " conversation must have a subject");
        }
        if (type != ConversationType.GROUP && concernsUnit != null) {
            throw new IllegalArgumentException("a " + type + " conversation must not concern a unit");
        }
        this.participantUserIds = participants;
        this.subject = subject;
        this.concernsUnit = concernsUnit;
        this.createdDate = createdDate;
    }

    public static Conversation createGroup(ConversationId id, EntityId propertyId, EntityId createdBy,
                                            Set<EntityId> participantUserIds, ConversationSubject subject, String concernsUnit) {
        return new Conversation(id, propertyId, ConversationType.GROUP, createdBy, participantUserIds, subject, concernsUnit, null);
    }

    public static Conversation createBroadcast(ConversationId id, EntityId propertyId, EntityId createdBy,
                                                ConversationSubject subject) {
        Objects.requireNonNull(subject, "a broadcast must have a subject");
        return new Conversation(id, propertyId, ConversationType.BROADCAST, createdBy, Set.of(), subject, null, null);
    }

    public static Conversation createBoardPrivate(ConversationId id, EntityId propertyId, EntityId createdBy,
                                                    ConversationSubject subject) {
        return new Conversation(id, propertyId, ConversationType.BOARD_PRIVATE, createdBy, Set.of(), subject, null, null);
    }

    public static Conversation reconstruct(ConversationId id, EntityId propertyId, ConversationType type,
                                            EntityId createdBy, Set<EntityId> participantUserIds, ConversationSubject subject,
                                            String concernsUnit, Instant createdDate) {
        return new Conversation(id, propertyId, type, createdBy, participantUserIds, subject, concernsUnit, createdDate);
    }

    public boolean hasParticipant(EntityId userId) {
        return participantUserIds.contains(userId);
    }

    public ConversationSubject getSubject() {
        return subject;
    }

    public String getConcernsUnit() {
        return concernsUnit;
    }

    public ConversationId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public ConversationType getType() {
        return type;
    }

    public EntityId getCreatedBy() {
        return createdBy;
    }

    public Set<EntityId> getParticipantUserIds() {
        return participantUserIds;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Conversation other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
