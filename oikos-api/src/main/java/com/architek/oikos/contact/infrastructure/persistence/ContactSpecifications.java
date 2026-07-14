package com.architek.oikos.contact.infrastructure.persistence;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.architek.oikos.contact.domain.valueobject.ContactSearchCriteria;

public final class ContactSpecifications {

    private ContactSpecifications() {
    }

    public static Specification<ContactEntity> matching(ContactSearchCriteria criteria) {
        Specification<ContactEntity> spec = Specification.allOf();
        if (criteria.search() != null && !criteria.search().isBlank()) {
            spec = spec.and(searchText(criteria.search()));
        }
        return spec;
    }

    private static Specification<ContactEntity> searchText(String search) {
        String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern));
    }
}
