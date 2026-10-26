package com.architek.oikos.document.infrastructure.persistence;

import java.util.Locale;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

public final class DocumentSpecifications {

    private DocumentSpecifications() {
    }

    public static Specification<DocumentEntity> matching(String ownerType, UUID ownerId, String search) {
        Specification<DocumentEntity> spec = ownedBy(ownerType, ownerId);
        if (search != null && !search.isBlank()) {
            spec = spec.and(searchText(search));
        }
        return spec;
    }

    private static Specification<DocumentEntity> ownedBy(String ownerType, UUID ownerId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("ownerType"), ownerType),
                cb.equal(root.get("ownerId"), ownerId));
    }

    /**
     * The file name stays searchable beside the title: it is what a syndic
     * remembers having picked on their own disk, even once the title says
     * something else.
     */
    private static Specification<DocumentEntity> searchText(String search) {
        String pattern = "%" + search.strip().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("fileName")), pattern));
    }
}
