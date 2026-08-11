package com.architek.oikos.messaging.infrastructure.persistence;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

/**
 * participantUserIds is the conversation_participant join table (composite
 * PK conversation_id/user_id, see V17__messaging.sql): one row per
 * (conversation, user), holding the 2..N participantUserIds of a GROUP
 * conversation - always empty for BROADCAST, whose membership is resolved
 * dynamically instead (see Conversation's javadoc). Same
 * @ElementCollection/@CollectionTable pattern as UserEntity's
 * linkedPartyIds/app_user_party.
 */
@Entity
@Table(name = "conversation")
@Getter
@Setter
@NoArgsConstructor
public class ConversationEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    /** GROUP conversation title (see ConversationSubject) - always null for BROADCAST. */
    @Column
    private String subject;

    /** Optional lot label the GROUP thread concerns (see Conversation's javadoc) - always null otherwise. */
    @Column(name = "concerns_unit")
    private String concernsUnit;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "conversation_participant", joinColumns = @JoinColumn(name = "conversation_id"))
    @Column(name = "user_id", nullable = false)
    private Set<UUID> participantUserIds = new HashSet<>();
}
