package com.architek.oikos.user.application.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;

public record UserView(UserId id, String fullName, String email, Set<Role> roles, Set<String> managedPropertyIds,
                        boolean verified, boolean enabled) {

    public static UserView of(User user) {
        Set<String> managedPropertyIds = user.getPropertyRoleGrants().stream()
                .filter(grant -> grant.role() == PropertyRole.ROLE_PROPERTY_MANAGER)
                .map(grant -> grant.propertyId().toString())
                .collect(Collectors.toSet());
        return new UserView(user.getId(), user.getFullName(), user.getEmail().value(),
                user.getRoles(), managedPropertyIds, user.isVerified(), user.isEnabled());
    }
}
