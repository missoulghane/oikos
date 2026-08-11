package com.architek.oikos.notification.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A single-recipient notice (never shared between several readers, unlike a
 * Conversation) surfaced through both a paginated inbox (ListMyNotificationsUseCase)
 * and its own individually consultable URL (GetNotificationUseCase,
 * GET /notifications/{id} - see arbitrage A3 in GAP.md: "une notification
 * doit avoir un URL qu'on peut consulter"). propertyId is nullable - most
 * notifications concern one property, but the type exists to also cover
 * eventual account-wide notices that don't. linkPath is an optional in-app
 * relative path (e.g. "/property-ownership/installments") the frontend
 * navigates to when the notification is opened - a display hint set by
 * whichever producer created it, never validated against the route table
 * here (same non-structural-link stance as Conversation.concernsUnit).
 *
 * <p>No producer creates a Notification yet as of this module's introduction
 * (see NotificationType's javadoc) - CreateNotificationUseCase exists so a
 * future domain event handler in another module can call it, but nothing
 * does today; the inbox is real infrastructure sitting empty until that
 * wiring lands.
 *
 * <p>Mutable read state (readAt), unlike Message/Conversation's full
 * immutability: markRead returns a new instance the same way
 * BoardMember.activate() does, rather than mutating in place - entity
 * semantics: equals/hashCode are identity-based.
 */
public final class Notification {

    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_BODY_LENGTH = 1000;
    private static final int MAX_LINK_PATH_LENGTH = 300;

    private final NotificationId id;
    private final EntityId recipientUserId;
    private final EntityId propertyId;
    private final NotificationType type;
    private final String title;
    private final String body;
    private final String linkPath;
    private final Instant readAt;
    private final Instant createdDate;

    private Notification(NotificationId id, EntityId recipientUserId, EntityId propertyId, NotificationType type,
                          String title, String body, String linkPath, Instant readAt, Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.recipientUserId = Objects.requireNonNull(recipientUserId, "recipientUserId must not be null");
        this.propertyId = propertyId;
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("title must not exceed " + MAX_TITLE_LENGTH + " characters");
        }
        if (body != null && body.length() > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException("body must not exceed " + MAX_BODY_LENGTH + " characters");
        }
        if (linkPath != null && linkPath.length() > MAX_LINK_PATH_LENGTH) {
            throw new IllegalArgumentException("linkPath must not exceed " + MAX_LINK_PATH_LENGTH + " characters");
        }
        this.body = body;
        this.linkPath = linkPath;
        this.readAt = readAt;
        this.createdDate = createdDate;
    }

    public static Notification create(NotificationId id, EntityId recipientUserId, EntityId propertyId, NotificationType type,
                                       String title, String body, String linkPath) {
        return new Notification(id, recipientUserId, propertyId, type, title, body, linkPath, null, null);
    }

    public static Notification reconstruct(NotificationId id, EntityId recipientUserId, EntityId propertyId, NotificationType type,
                                            String title, String body, String linkPath, Instant readAt, Instant createdDate) {
        return new Notification(id, recipientUserId, propertyId, type, title, body, linkPath, readAt, createdDate);
    }

    /** Idempotent: returns this unchanged if already read, rather than overwriting readAt on a second call. */
    public Notification markRead(Instant now) {
        if (isRead()) {
            return this;
        }
        return new Notification(id, recipientUserId, propertyId, type, title, body, linkPath,
                Objects.requireNonNull(now, "now must not be null"), createdDate);
    }

    public boolean isRead() {
        return readAt != null;
    }

    public NotificationId getId() {
        return id;
    }

    public EntityId getRecipientUserId() {
        return recipientUserId;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getLinkPath() {
        return linkPath;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Notification other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
