package com.architek.oikos.property.domain.exception;

import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ErrorCodes;

public class UnitTypeInUseException extends BusinessException implements CodedException {

    public UnitTypeInUseException(UnitTypeDefinitionId id) {
        super("Unit type " + id + " is still assigned to at least one unit and cannot be removed");
    }

    /**
     * Un refus que l'utilisateur peut lever lui-même (retirer le type des lots qui
     * le portent) : l'écran a donc besoin de le distinguer d'un conflit quelconque.
     */
    @Override
    public String errorCode() {
        return ErrorCodes.UNIT_TYPE_IN_USE;
    }
}
