package com.architek.oikos.user.application.dto;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;

public record UserView(UserId id, String fullName, String email, String phone, Set<Role> roles,
                        Map<String, Set<PropertyRole>> roleByProperty, boolean verified, boolean enabled,
                        boolean hasAvatar) {

    /**
     * A caller can hold more than one grant on the same property (e.g. a board member
     * who is also a unit owner) and more than one board mandate across different
     * properties: the full set is exposed per property, rather than collapsing to a
     * single "most privileged" role, so callers (oikos-web's access.ts, the space
     * switcher) can tell "owner AND board member here" from either alone.
     */
    public static UserView of(User user) {
        Map<String, Set<PropertyRole>> roleByProperty = user.getPropertyRoleGrants().stream()
                .collect(Collectors.groupingBy(
                        grant -> grant.propertyId().toString(),
                        Collectors.mapping(grant -> grant.role(), Collectors.toSet())));
        return new UserView(user.getId(), user.getFullName(), user.getEmail().value(), user.getPhone(),
                user.getRoles(), roleByProperty, user.isVerified(), user.isEnabled(), user.hasAvatar());
    }
}
