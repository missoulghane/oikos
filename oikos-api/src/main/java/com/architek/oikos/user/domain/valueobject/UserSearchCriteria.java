package com.architek.oikos.user.domain.valueobject;

import com.architek.oikos.user.domain.model.Role;

/**
 * Optional list-filter axes for admin user search: null means "no filter on this
 * axis". search matches the account login only - name/email search now belongs to
 * the party feature (see party.infrastructure.persistence.PartySpecifications).
 */
public record UserSearchCriteria(String search, Role role, Boolean enabled) {

    public static UserSearchCriteria empty() {
        return new UserSearchCriteria(null, null, null);
    }
}
