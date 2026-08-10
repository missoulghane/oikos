package com.architek.oikos.messaging.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A message-in-progress, owned by exactly one user (createdBy) - never
 * visible to anyone else, never a participant list in the
 * conversation_participant sense. Unlike Conversation/Message, a draft may
 * be genuinely incomplete: recipientUserIds may be empty, subject/body may
 * be null - it only means "not sent yet", not "invalid". The one invariant
 * mirrored from Conversation is that isBroadcast == true implies no stored
 * recipients (a broadcast draft's "recipient" is always the whole property,
 * resolved the same way a real BROADCAST conversation resolves it - see
 * SendMessageDraftService). Real validation (non-blank subject/body,
 * recipients still valid property members, still holding broadcast
 * permission) only happens when the draft is actually sent, by reusing
 * StartGroupConversationService/SendBroadcastMessageService rather than
 * duplicating their rules here.
 *
 * <p>Immutable: update(...) returns a new instance with a refreshed
 * lastModifiedDate, same convention as MembershipRequest.accept/reject.
 */
public final class MessageDraft {

    private final MessageDraftId id;
    private final EntityId propertyId;
    private final EntityId createdBy;
    private final Set<EntityId> recipientUserIds;
    private final boolean broadcast;
    private final String subject;
    private final String body;
    private final Instant createdDate;
    private final Instant lastModifiedDate;

    private MessageDraft(MessageDraftId id, EntityId propertyId, EntityId createdBy, Set<EntityId> recipientUserIds,
                          boolean broadcast, String subject, String body, Instant createdDate, Instant lastModifiedDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy must not be null");
        Set<EntityId> recipients = Set.copyOf(Objects.requireNonNull(recipientUserIds, "recipientUserIds must not be null"));
        if (broadcast && !recipients.isEmpty()) {
            throw new IllegalArgumentException("a broadcast draft must not store recipients");
        }
        this.recipientUserIds = recipients;
        this.broadcast = broadcast;
        this.subject = subject;
        this.body = body;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static MessageDraft create(MessageDraftId id, EntityId propertyId, EntityId createdBy,
                                       Set<EntityId> recipientUserIds, boolean broadcast, String subject, String body) {
        return new MessageDraft(id, propertyId, createdBy, recipientUserIds, broadcast, subject, body, null, null);
    }

    public static MessageDraft reconstruct(MessageDraftId id, EntityId propertyId, EntityId createdBy,
                                            Set<EntityId> recipientUserIds, boolean broadcast, String subject, String body,
                                            Instant createdDate, Instant lastModifiedDate) {
        return new MessageDraft(id, propertyId, createdBy, recipientUserIds, broadcast, subject, body, createdDate,
                lastModifiedDate);
    }

    public MessageDraft update(Set<EntityId> recipientUserIds, boolean broadcast, String subject, String body,
                                Instant now) {
        return new MessageDraft(id, propertyId, createdBy, recipientUserIds, broadcast, subject, body, createdDate, now);
    }

    public MessageDraftId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public EntityId getCreatedBy() {
        return createdBy;
    }

    public Set<EntityId> getRecipientUserIds() {
        return recipientUserIds;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof MessageDraft other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
