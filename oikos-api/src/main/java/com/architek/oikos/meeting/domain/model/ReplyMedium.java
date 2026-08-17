package com.architek.oikos.meeting.domain.model;

import java.util.Objects;

import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;

/**
 * A means by which an answer reached the syndic's office - phone, post, email,
 * counter - as a catalog row.
 *
 * <p>Same shape as {@link ConvocationChannel}, minus its {@code automated}
 * flag, and the absence is the point: a channel can be something the
 * application performs, a medium never is. Nothing is ever emitted through one;
 * it only records how something arrived. That makes the catalog cheap in a way
 * the channels are not - a new medium costs one INSERT and nothing else, where
 * a new automated channel also needs an emitter.
 *
 * <p>The catalog is global, not scoped by property: the means are the
 * product's, not one copropriété's.
 *
 * <p>Immutable. Entity semantics: equals/hashCode are identity-based.
 */
public final class ReplyMedium {

    private final ReplyMediumCode code;
    private final String label;
    private final int position;
    private final boolean active;

    private ReplyMedium(ReplyMediumCode code, String label, int position, boolean active) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.label = Objects.requireNonNull(label, "label must not be null");
        if (label.isBlank()) {
            throw new IllegalArgumentException("reply medium label must not be blank");
        }
        this.position = position;
        this.active = active;
    }

    public static ReplyMedium of(ReplyMediumCode code, String label, int position, boolean active) {
        return new ReplyMedium(code, label, position, active);
    }

    public ReplyMediumCode getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public int getPosition() {
        return position;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof ReplyMedium other && code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
