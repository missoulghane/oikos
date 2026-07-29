package com.architek.oikos.user.infrastructure.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionJpaRepository
        extends JpaRepository<RolePermissionEntity, RolePermissionEntity.RolePermissionKey> {

    List<RolePermissionEntity> findByRoleNameIn(Collection<String> roleNames);
}
