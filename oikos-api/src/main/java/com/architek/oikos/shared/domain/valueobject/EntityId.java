package com.architek.oikos.shared.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Generic identifier value object encapsulating a UUID. Feature-specific id types
 * (UserId, LineId, StopId, ...) wrap this type by composition rather than inheritance,
 * since domain identifiers are immutable final records.
 */
public record EntityId(UUID value) {

    public EntityId {
        Objects.requireNonNull(value, "id value must not be null");
    }

    public static EntityId newId() {
        return new EntityId(UUID.randomUUID());
    }

    public static EntityId of(UUID value) {
        return new EntityId(value);
    }

    public static EntityId of(String value) {
        Objects.requireNonNull(value, "id value must not be null");
        try {
            return new EntityId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid id format: " + value, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
