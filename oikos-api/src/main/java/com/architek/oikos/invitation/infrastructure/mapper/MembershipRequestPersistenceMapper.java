package com.architek.oikos.invitation.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.invitation.infrastructure.persistence.MembershipRequestEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface MembershipRequestPersistenceMapper {

    default MembershipRequestEntity toEntity(MembershipRequest request, MembershipRequestEntity entity) {
        entity.setId(request.getId().asUuid());
        entity.setInvitationId(request.getInvitationId().value());
        entity.setPropertyId(request.getPropertyId().value());
        entity.setUnitId(request.getUnitId().value());
        entity.setPartyId(request.getPartyId().value());
        entity.setUserId(request.getUserId().value());
        entity.setStatus(request.getStatus());
        entity.setDecidedAt(request.getDecidedAt());
        entity.setDecidedByUserId(request.getDecidedByUserId() != null ? request.getDecidedByUserId().value() : null);
        entity.setRejectionReason(request.getRejectionReason());
        return entity;
    }

    default MembershipRequest toDomain(MembershipRequestEntity entity) {
        return MembershipRequest.reconstruct(MembershipRequestId.of(entity.getId()), EntityId.of(entity.getInvitationId()),
                EntityId.of(entity.getPropertyId()), EntityId.of(entity.getUnitId()), EntityId.of(entity.getPartyId()),
                EntityId.of(entity.getUserId()), entity.getStatus(), entity.getDecidedAt(),
                entity.getDecidedByUserId() != null ? EntityId.of(entity.getDecidedByUserId()) : null,
                entity.getRejectionReason(), entity.getCreatedDate());
    }
}
