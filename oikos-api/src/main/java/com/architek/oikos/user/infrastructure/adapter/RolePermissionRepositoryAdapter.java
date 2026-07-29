package com.architek.oikos.user.infrastructure.adapter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.architek.oikos.user.domain.model.Permission;
import com.architek.oikos.user.domain.repository.RolePermissionRepository;
import com.architek.oikos.user.infrastructure.persistence.RolePermissionEntity;
import com.architek.oikos.user.infrastructure.persistence.RolePermissionJpaRepository;

@Component
public class RolePermissionRepositoryAdapter implements RolePermissionRepository {

    private final RolePermissionJpaRepository jpaRepository;

    public RolePermissionRepositoryAdapter(RolePermissionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Map<String, Set<Permission>> findPermissionsByRoleNames(Set<String> roleNames) {
        return jpaRepository.findByRoleNameIn(roleNames).stream()
                .collect(Collectors.groupingBy(
                        RolePermissionEntity::getRoleName,
                        Collectors.mapping(entity -> Permission.fromKey(entity.getPermissionKey()), Collectors.toSet())));
    }
}
