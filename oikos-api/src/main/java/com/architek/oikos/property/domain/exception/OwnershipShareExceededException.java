package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.exception.BusinessException;

/**
 * RG: la somme des OwnershipShare d'un meme unit ne peut pas depasser 100%.
 */
public class OwnershipShareExceededException extends BusinessException {

    public OwnershipShareExceededException(UnitId unitId) {
        super("Total ownership shares for unit " + unitId + " would exceed 100%");
    }
}
