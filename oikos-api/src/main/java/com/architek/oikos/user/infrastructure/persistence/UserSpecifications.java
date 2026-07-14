package com.architek.oikos.user.infrastructure.persistence;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.valueobject.UserSearchCriteria;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<UserEntity> matching(UserSearchCriteria criteria) {
        Specification<UserEntity> spec = Specification.allOf();
        if (criteria.search() != null && !criteria.search().isBlank()) {
            spec = spec.and(searchText(criteria.search()));
        }
        if (criteria.role() != null) {
            spec = spec.and(hasRole(criteria.role()));
        }
        if (criteria.enabled() != null) {
            spec = spec.and(isEnabled(criteria.enabled()));
        }
        return spec;
    }

    private static Specification<UserEntity> searchText(String search) {
        String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("login")), pattern);
    }

    private static Specification<UserEntity> hasRole(Role role) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.join("roles"), role.name());
        };
    }

    private static Specification<UserEntity> isEnabled(boolean enabled) {
        return (root, query, cb) -> cb.equal(root.get("enabled"), enabled);
    }
}
