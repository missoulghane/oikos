package com.architek.oikos.user.application.dto;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;

public record UserView(UserId id, String fullName, String email, Set<Role> roles,
                        Map<String, PropertyRole> roleByProperty, boolean verified, boolean enabled) {

    public static UserView of(User user) {
        Map<String, PropertyRole> roleByProperty = user.getPropertyRoleGrants().stream()
                .collect(Collectors.toMap(
                        grant -> grant.propertyId().toString(),
                        grant -> grant.role(),
                        // A user should hold at most one role per property; if more than one
                        // grant ever collides on the same property, keep the ADMIN-tier one.
                        (existing, incoming) -> existing.isAdminTier() ? existing : incoming));
        return new UserView(user.getId(), user.getFullName(), user.getEmail().value(),
                user.getRoles(), roleByProperty, user.isVerified(), user.isEnabled());
    }
}
