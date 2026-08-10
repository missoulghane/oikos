package com.architek.oikos.messaging.infrastructure.persistence;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

/**
 * recipientUserIds is the message_draft_recipient join table (composite PK,
 * see V18__message_drafts.sql) - same @ElementCollection/@CollectionTable
 * pattern as ConversationEntity.participantUserIds, always empty when
 * isBroadcast is true.
 */
@Entity
@Table(name = "message_draft")
@Getter
@Setter
@NoArgsConstructor
public class MessageDraftEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "is_broadcast", nullable = false)
    private boolean broadcast;

    @Column(length = 200)
    private String subject;

    // length matches MessageBody's domain cap (see MessageBody.java, applied at send time) -
    // without it Hibernate defaults to varchar(255) generating the dev/H2 schema from these
    // annotations, silently narrower than the real migration's TEXT column
    // (V18__message_drafts.sql) - same bug as MessageEntity.body.
    @Column(length = 4000)
    private String body;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "message_draft_recipient", joinColumns = @JoinColumn(name = "message_draft_id"))
    @Column(name = "user_id", nullable = false)
    private Set<UUID> recipientUserIds = new HashSet<>();
}
