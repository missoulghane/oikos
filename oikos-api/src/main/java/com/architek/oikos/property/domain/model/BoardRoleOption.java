package com.architek.oikos.property.domain.model;

import java.util.Objects;

import com.architek.oikos.property.domain.valueobject.BoardRole;

/**
 * A seat on a property's board as a catalog row: the label every screen shows
 * for it, its place in the list, and whether a volunteer syndic may claim it
 * when signing up.
 *
 * <p>The seats themselves stay a closed enum ({@link BoardRole}) - rules branch
 * on them. What the catalog owns is presentation: wording and order, changed by
 * an UPDATE rather than a release of every client.
 *
 * <p>Global, not scoped by property. Immutable; equals/hashCode on the code.
 */
public final class BoardRoleOption {

    private final BoardRole code;
    private final String label;
    private final int position;
    private final boolean availableAtSignup;
    private final boolean active;

    private BoardRoleOption(BoardRole code, String label, int position, boolean availableAtSignup, boolean active) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.label = Objects.requireNonNull(label, "label must not be null");
        if (label.isBlank()) {
            throw new IllegalArgumentException("board role label must not be blank");
        }
        this.position = position;
        this.availableAtSignup = availableAtSignup;
        this.active = active;
    }

    public static BoardRoleOption of(BoardRole code, String label, int position, boolean availableAtSignup,
                                     boolean active) {
        return new BoardRoleOption(code, label, position, availableAtSignup, active);
    }

    public BoardRole getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public int getPosition() {
        return position;
    }

    public boolean isAvailableAtSignup() {
        return availableAtSignup;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof BoardRoleOption other && code == other.code;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
