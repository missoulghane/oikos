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
 * memberUserIds is the recipient_group_member join table (V8), same
 * @ElementCollection/@CollectionTable pattern as ConversationEntity's
 * participantUserIds.
 */
@Entity
@Table(name = "recipient_group")
@Getter
@Setter
@NoArgsConstructor
public class RecipientGroupEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipient_group_member", joinColumns = @JoinColumn(name = "recipient_group_id"))
    @Column(name = "user_id", nullable = false)
    private Set<UUID> memberUserIds = new HashSet<>();
}
