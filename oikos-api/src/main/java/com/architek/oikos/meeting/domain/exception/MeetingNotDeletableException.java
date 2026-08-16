package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.shared.exception.ConflictException;

/**
 * Deleting a meeting the copropriétaires have already been convoked to. Its
 * convocations, votes and minutes would go with it (ON DELETE CASCADE) - that
 * is destruction, not correction, which is why this guard survives the general
 * relaxation of the editing rules (ADR 0002 §8).
 */
public class MeetingNotDeletableException extends ConflictException {

    public MeetingNotDeletableException(MeetingStatus status) {
        super("A general meeting can only be deleted before its convocations go out, current status: " + status);
    }
}
