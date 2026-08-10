package com.architek.oikos.messaging.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Immutable history record - no AuditableEntity (no last_modified_date/
 * version, a message is never updated, see Message's own javadoc) - only
 * createdDate, set application-side at Message.post(...) time rather than by
 * JPA auditing, so ordering/unread computation don't depend on a round-trip.
 */
@Entity
@Table(name = "message")
@Getter
@Setter
@NoArgsConstructor
public class MessageEntity {

    @Id
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    // length matches MessageBody's domain cap (see MessageBody.java) - without it Hibernate
    // defaults to varchar(255) when generating the dev/H2 schema from these annotations
    // (ddl-auto: create-drop), silently narrower than the real migration's TEXT column
    // (V17__messaging.sql) and than what the domain/request DTO actually validate.
    @Column(nullable = false, length = 4000)
    private String body;

    @Column(name = "created_date", nullable = false)
    private Instant createdDate;
}
