package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/** A vote for a lot that was never convoked to this meeting - it belongs to another copropriété, or to none. */
public class UnitNotConvokedException extends BusinessException {

    public UnitNotConvokedException(String unitId) {
        super("Lot " + unitId + " has no convocation for this general meeting");
    }
}
