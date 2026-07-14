package com.architek.oikos.user.infrastructure.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.infrastructure.persistence.UserEntity;

/**
 * Domain <-> entity mapping. Implemented as default methods rather than
 * auto-generated field mapping since the domain side is composed of value objects
 * (EntityId, HashedPassword, UserId) that need explicit unwrapping.
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
        entity.setContactId(user.getContactId().value());
        entity.setPasswordHash(user.getPassword().value());
        entity.setLogin(user.getLogin());
        entity.setRoles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
        entity.setVerified(user.isVerified());
        entity.setEnabled(user.isEnabled());
        return entity;
    }

    default User toDomain(UserEntity entity) {
        Set<Role> roles = entity.getRoles().stream().map(Role::valueOf).collect(Collectors.toSet());
        return User.reconstruct(
                UserId.of(entity.getId()),
                EntityId.of(entity.getContactId()),
                HashedPassword.of(entity.getPasswordHash()),
                entity.getLogin(),
                roles,
                entity.isVerified(),
                entity.isEnabled());
    }
}
