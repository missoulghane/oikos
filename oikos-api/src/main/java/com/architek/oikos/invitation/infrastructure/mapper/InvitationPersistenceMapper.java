package com.architek.oikos.invitation.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.invitation.infrastructure.persistence.InvitationEntity;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface InvitationPersistenceMapper {

    default InvitationEntity toEntity(Invitation invitation, InvitationEntity entity) {
        entity.setId(invitation.getId().asUuid());
        entity.setPropertyId(invitation.getPropertyId().value());
        entity.setType(invitation.getType());
        entity.setTargetRole(invitation.getTargetRole());
        entity.setTargetEmail(invitation.getTargetEmail() != null ? invitation.getTargetEmail().value() : null);
        entity.setToken(invitation.getToken());
        entity.setStatus(invitation.getStatus());
        entity.setExpiresAt(invitation.getExpiresAt());
        entity.setCreatedByUserId(invitation.getCreatedByUserId().value());
        entity.setConsumedEmail(invitation.getConsumedEmail() != null ? invitation.getConsumedEmail().value() : null);
        entity.setTargetBoardRole(invitation.getTargetBoardRole());
        return entity;
    }

    default Invitation toDomain(InvitationEntity entity) {
        return Invitation.reconstruct(InvitationId.of(entity.getId()), EntityId.of(entity.getPropertyId()),
                entity.getType(), entity.getTargetRole(),
                entity.getTargetEmail() != null ? EmailVO.of(entity.getTargetEmail()) : null,
                entity.getToken(), entity.getStatus(), entity.getExpiresAt(), EntityId.of(entity.getCreatedByUserId()),
                entity.getConsumedEmail() != null ? EmailVO.of(entity.getConsumedEmail()) : null,
                entity.getTargetBoardRole());
    }
}
