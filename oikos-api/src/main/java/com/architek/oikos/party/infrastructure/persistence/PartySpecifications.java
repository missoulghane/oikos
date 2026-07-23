package com.architek.oikos.party.infrastructure.persistence;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;

public final class PartySpecifications {

    private PartySpecifications() {
    }

    public static Specification<PartyEntity> matching(PartySearchCriteria criteria) {
        Specification<PartyEntity> spec = Specification.allOf();
        if (criteria.search() != null && !criteria.search().isBlank()) {
            spec = spec.and(searchText(criteria.search()));
        }
        return spec;
    }

    private static Specification<PartyEntity> searchText(String search) {
        String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("fullName")), pattern));
    }
}
