package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;

/** A unit has at most one unit_account (spec &sect;7: one per lot). */
public class DuplicateUnitAccountException extends BusinessException {

    public DuplicateUnitAccountException(EntityId unitId) {
        super("A unit account already exists for unit: " + unitId);
    }
}
