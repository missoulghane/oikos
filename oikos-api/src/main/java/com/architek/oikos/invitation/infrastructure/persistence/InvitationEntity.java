package com.architek.oikos.invitation.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.shared.infrastructure.audit.AuditableEntity;

@Entity
@Table(name = "invitation")
@Getter
@Setter
@NoArgsConstructor
public class InvitationEntity extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationType type;

    @Column(name = "target_role", nullable = false)
    private String targetRole;

    @Column(name = "target_email")
    private String targetEmail;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "consumed_email")
    private String consumedEmail;

    @Column(name = "target_board_role")
    private String targetBoardRole;

    @Column(name = "target_unit_id")
    private UUID targetUnitId;

    @Column(name = "target_party_id")
    private UUID targetPartyId;
}
