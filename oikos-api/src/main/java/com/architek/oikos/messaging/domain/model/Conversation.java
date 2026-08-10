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
 * (ListMyConversationsUseCase), unrelated to creation. BROADCAST
 * conversations are unique per property (uk_conversation_broadcast_property),
 * carry no stored participants and no subject: their identity is already the
 * fixed channel label rendered by the frontend, not a per-message title.
 * Membership is resolved dynamically from the property's current roster at
 * read time (UserAccessPort/PropertyMemberDirectoryPort), so someone who
 * joins the property later immediately sees the channel's history, and
 * someone who leaves immediately loses access - nothing to migrate either
 * way.
 *
 * <p>createdDate mirrors the persistence layer's audited created_date
 * (read-only from the domain's point of view - AuditableEntity/JPA auditing
 * is the sole writer): null on a freshly created, not-yet-persisted instance,
 * populated by the mapper once reloaded from a saved entity.
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
    private final Instant createdDate;

    private Conversation(ConversationId id, EntityId propertyId, ConversationType type, EntityId createdBy,
                          Set<EntityId> participantUserIds, ConversationSubject subject, Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy must not be null");
        Set<EntityId> participants = Set.copyOf(Objects.requireNonNull(participantUserIds, "participantUserIds must not be null"));
        if (type == ConversationType.GROUP && participants.size() < 2) {
            throw new IllegalArgumentException("a GROUP conversation must have at least 2 participants");
        }
        if (type == ConversationType.BROADCAST && !participants.isEmpty()) {
            throw new IllegalArgumentException("a BROADCAST conversation must not store participants");
        }
        if (type == ConversationType.GROUP && subject == null) {
            throw new IllegalArgumentException("a GROUP conversation must have a subject");
        }
        if (type == ConversationType.BROADCAST && subject != null) {
            throw new IllegalArgumentException("a BROADCAST conversation must not have a subject");
        }
        this.participantUserIds = participants;
        this.subject = subject;
        this.createdDate = createdDate;
    }

    public static Conversation createGroup(ConversationId id, EntityId propertyId, EntityId createdBy,
                                            Set<EntityId> participantUserIds, ConversationSubject subject) {
        return new Conversation(id, propertyId, ConversationType.GROUP, createdBy, participantUserIds, subject, null);
    }

    public static Conversation createBroadcast(ConversationId id, EntityId propertyId, EntityId createdBy) {
        return new Conversation(id, propertyId, ConversationType.BROADCAST, createdBy, Set.of(), null, null);
    }

    public static Conversation reconstruct(ConversationId id, EntityId propertyId, ConversationType type,
                                            EntityId createdBy, Set<EntityId> participantUserIds, ConversationSubject subject,
                                            Instant createdDate) {
        return new Conversation(id, propertyId, type, createdBy, participantUserIds, subject, createdDate);
    }

    public boolean hasParticipant(EntityId userId) {
        return participantUserIds.contains(userId);
    }

    public ConversationSubject getSubject() {
        return subject;
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
