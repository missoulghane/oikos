package com.architek.oikos.property.domain.model;

import java.util.Objects;

/**
 * A unit type offered wherever one is picked - the sign-up wizard and a
 * property's configuration - as a catalog row.
 *
 * <p>Not a {@link UnitTypeDefinition}: that one is a property's own type, priced
 * and attached to its units. A choice is only what the product suggests; the
 * definition copies its name. A property that already carries a type off the
 * list keeps it.
 *
 * <p>The plural serves the summaries ("12 appartements · 8 box"), where the
 * singular would read wrong. Immutable; equals/hashCode on the name.
 */
public final class UnitTypeChoice {

    private final String name;
    private final String plural;
    private final String description;
    private final int position;
    private final boolean active;

    private UnitTypeChoice(String name, String plural, String description, int position, boolean active) {
        this.name = requireText(name, "name");
        this.plural = requireText(plural, "plural");
        this.description = requireText(description, "description");
        this.position = position;
        this.active = active;
    }

    public static UnitTypeChoice of(String name, String plural, String description, int position, boolean active) {
        return new UnitTypeChoice(name, plural, description, position, active);
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("unit type choice " + field + " must not be blank");
        }
        return value;
    }

    public String getName() {
        return name;
    }

    public String getPlural() {
        return plural;
    }

    public String getDescription() {
        return description;
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
        return o instanceof UnitTypeChoice other && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
