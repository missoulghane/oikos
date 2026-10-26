package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.ConflictException;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * The wizard's step 2 was replayed by an account that already administers a
 * property. Guarding on the existing grant is what makes the step safe to
 * retry (double submit, back button): it never creates a second property, and
 * a volunteer syndic manages a single one - more is the professional track.
 */
public class BoardAdminPropertyAlreadyExistsException extends ConflictException {

    public BoardAdminPropertyAlreadyExistsException(UserId userId) {
        super("User " + userId + " already administers a property");
    }
}
