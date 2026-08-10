package com.architek.oikos.messaging.infrastructure.persistence;

import java.util.Locale;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

public final class MessageDraftSpecifications {

    private MessageDraftSpecifications() {
    }

    public static Specification<MessageDraftEntity> matching(UUID createdBy, String search) {
        Specification<MessageDraftEntity> spec = hasCreatedBy(createdBy);
        if (search != null && !search.isBlank()) {
            spec = spec.and(searchText(search));
        }
        return spec;
    }

    private static Specification<MessageDraftEntity> hasCreatedBy(UUID createdBy) {
        return (root, query, cb) -> cb.equal(root.get("createdBy"), createdBy);
    }

    private static Specification<MessageDraftEntity> searchText(String search) {
        String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("subject")), pattern),
                cb.like(cb.lower(root.get("body")), pattern));
    }
}
