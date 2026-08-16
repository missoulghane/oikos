package com.architek.oikos.meeting.domain.valueobject;

import java.util.Set;

/**
 * The three states of the SFD (brouillon / en_validation / publié).
 *
 * <p>UNDER_REVIEW is the SFD's own wording and is kept for that reason, but it
 * means "validated, content locked, awaiting publication" - not "someone is
 * still reading it". Validating is precisely what stops the editing; what
 * remains after it is the decision to diffuse.
 *
 * <p>No way back, deliberately. Minutes sent to every copropriétaire cannot be
 * unsent, and a record that could be edited after publication would not be a
 * record. Correcting published minutes is an act for the next assembly, not a
 * database update.
 */
public enum MinutesStatus {

    /** Generated from the session's own data; the only state where the text can be edited. */
    DRAFT,
    /** Validated: the content is frozen and nothing but publication may follow. */
    UNDER_REVIEW,
    /** Diffused to the copropriétaires - end of the cycle. */
    PUBLISHED;

    public boolean canTransitionTo(MinutesStatus target) {
        return switch (this) {
            case DRAFT -> Set.of(UNDER_REVIEW).contains(target);
            case UNDER_REVIEW -> Set.of(PUBLISHED).contains(target);
            case PUBLISHED -> false;
        };
    }

    public boolean isEditable() {
        return this == DRAFT;
    }
}
