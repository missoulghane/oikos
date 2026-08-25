package com.architek.oikos.messaging.infrastructure.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import com.architek.oikos.messaging.domain.model.RecipientGroup;
import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.messaging.infrastructure.persistence.RecipientGroupEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface RecipientGroupPersistenceMapper {

    default RecipientGroupEntity toEntity(RecipientGroup group, RecipientGroupEntity entity) {
        entity.setId(group.getId().asUuid());
        entity.setPropertyId(group.getPropertyId().value());
        entity.setName(group.getName());
        entity.setCreatedBy(group.getCreatedBy().value());
        entity.setMemberUserIds(group.getMemberUserIds().stream().map(EntityId::value).collect(Collectors.toSet()));
        return entity;
    }

    default RecipientGroup toDomain(RecipientGroupEntity entity) {
        Set<EntityId> memberUserIds = entity.getMemberUserIds().stream().map(EntityId::of).collect(Collectors.toSet());
        return RecipientGroup.reconstruct(RecipientGroupId.of(entity.getId()), EntityId.of(entity.getPropertyId()),
                entity.getName(), memberUserIds, EntityId.of(entity.getCreatedBy()), entity.getCreatedDate());
    }
}
