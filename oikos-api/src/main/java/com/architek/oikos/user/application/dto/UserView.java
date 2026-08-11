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
                        // A caller can hold more than one grant on the same property (e.g. a
                        // board member who is also a unit owner - see oikos-web's
                        // canManageProperties, which this map feeds): keep the most
                        // privileged one, ADMIN-tier > MEMBER-tier > OWNER, rather than
                        // whichever grant the backing HashSet happens to iterate last.
                        (existing, incoming) -> rank(incoming) > rank(existing) ? incoming : existing));
        return new UserView(user.getId(), user.getFullName(), user.getEmail().value(),
                user.getRoles(), roleByProperty, user.isVerified(), user.isEnabled());
    }

    private static int rank(PropertyRole role) {
        if (role.isAdminTier()) {
            return 2;
        }
        return role == PropertyRole.PROPERTY_OWNER ? 0 : 1;
    }
}
