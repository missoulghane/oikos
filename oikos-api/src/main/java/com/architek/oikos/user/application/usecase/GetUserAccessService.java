package com.architek.oikos.user.application.usecase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.application.query.GetUserAccessQuery;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.Permission;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.PropertyRoleGrant;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.RolePermissionRepository;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class GetUserAccessService implements GetUserAccessUseCase {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public GetUserAccessService(UserRepository userRepository, RolePermissionRepository rolePermissionRepository) {
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccessView getAccess(GetUserAccessQuery query) {
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException(query.userId()));

        Set<String> globalRoleNames = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
        Set<String> allRoleNames = new HashSet<>(globalRoleNames);
        user.getPropertyRoleGrants().forEach(grant -> allRoleNames.add(grant.role().name()));

        Map<String, Set<Permission>> permissionsByRoleName = rolePermissionRepository.findPermissionsByRoleNames(allRoleNames);

        Set<Permission> globalPermissions = globalRoleNames.stream()
                .flatMap(role -> permissionsByRoleName.getOrDefault(role, Set.of()).stream())
                .collect(Collectors.toSet());

        Map<String, Set<PropertyRole>> rolesByProperty = new HashMap<>();
        Map<String, Set<Permission>> permissionsByProperty = new HashMap<>();
        for (PropertyRoleGrant grant : user.getPropertyRoleGrants()) {
            String propertyId = grant.propertyId().toString();
            rolesByProperty.computeIfAbsent(propertyId, id -> new HashSet<>()).add(grant.role());
            permissionsByProperty.computeIfAbsent(propertyId, id -> new HashSet<>())
                    .addAll(permissionsByRoleName.getOrDefault(grant.role().name(), Set.of()));
        }

        Set<String> ownedPartyIds = user.getLinkedPartyIds().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        return new UserAccessView(globalRoleNames, rolesByProperty, permissionsByProperty, globalPermissions, ownedPartyIds);
    }
}
