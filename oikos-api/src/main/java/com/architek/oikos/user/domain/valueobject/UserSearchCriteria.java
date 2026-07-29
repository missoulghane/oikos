package com.architek.oikos.user.domain.valueobject;

import com.architek.oikos.user.domain.model.Role;

/**
 * Optional list-filter axes for admin user search: null means "no filter on this
 * axis". search matches the account's own email.
 */
public record UserSearchCriteria(String search, Role role, Boolean enabled) {

    public static UserSearchCriteria empty() {
        return new UserSearchCriteria(null, null, null);
    }
}
