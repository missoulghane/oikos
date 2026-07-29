package com.architek.oikos.user.infrastructure.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.PropertyRoleGrant;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.infrastructure.persistence.PropertyRoleGrantEmbeddable;
import com.architek.oikos.user.infrastructure.persistence.UserEntity;

/**
 * Domain <-> entity mapping. Implemented as default methods rather than
 * auto-generated field mapping since the domain side is composed of value objects
 * (EmailVO, HashedPassword, UserId, EntityId) that need explicit unwrapping.
 */
@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    default UserEntity toEntity(User user) {
        return toEntity(user, new UserEntity());
    }

    /**
     * Populates an existing (possibly already-managed) entity instance rather than
     * always allocating a new one, so that repository adapters can update in place:
     * a freshly-allocated entity has a null @Version, which Spring Data JPA reads as
     * "new" and would attempt an INSERT instead of an UPDATE for an already-persisted
     * aggregate.
     */
    default UserEntity toEntity(User user, UserEntity entity) {
        entity.setId(user.getId().asUuid());
        entity.setEmail(user.getEmail().value());
        entity.setFullName(user.getFullName());
        entity.setPasswordHash(user.getPassword().value());
        entity.setRoles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
        entity.setLinkedPartyIds(user.getLinkedPartyIds().stream().map(EntityId::value).collect(Collectors.toSet()));
        entity.setPropertyRoleGrants(user.getPropertyRoleGrants().stream()
                .map(grant -> new PropertyRoleGrantEmbeddable(grant.partyId().value(), grant.propertyId().value(), grant.role().name()))
                .collect(Collectors.toSet()));
        entity.setVerified(user.isVerified());
        entity.setEnabled(user.isEnabled());
        return entity;
    }

    default User toDomain(UserEntity entity) {
        Set<Role> roles = entity.getRoles().stream().map(Role::valueOf).collect(Collectors.toSet());
        Set<EntityId> linkedPartyIds = entity.getLinkedPartyIds().stream().map(EntityId::of).collect(Collectors.toSet());
        Set<PropertyRoleGrant> propertyRoleGrants = entity.getPropertyRoleGrants().stream()
                .map(grant -> new PropertyRoleGrant(EntityId.of(grant.getPartyId()), EntityId.of(grant.getPropertyId()),
                        PropertyRole.valueOf(grant.getRole())))
                .collect(Collectors.toSet());
        return User.reconstruct(
                UserId.of(entity.getId()),
                EmailVO.of(entity.getEmail()),
                entity.getFullName(),
                HashedPassword.of(entity.getPasswordHash()),
                roles,
                linkedPartyIds,
                propertyRoleGrants,
                entity.isVerified(),
                entity.isEnabled());
    }
}
