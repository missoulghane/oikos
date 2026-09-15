package com.architek.oikos.property.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ErrorCodes;

public class PartyAlreadyOwnsUnitException extends BusinessException implements CodedException {

    public PartyAlreadyOwnsUnitException() {
        super("This party is already registered as an owner of this unit; remove the existing entry to change its share");
    }

    /**
     * Le seul refus de ce formulaire que l'écran sait expliquer mieux que l'API :
     * il le reconnaît à ce code (et non, comme avant, au texte anglais du message,
     * qu'un simple reformulage suffisait à faire échouer).
     */
    @Override
    public String errorCode() {
        return ErrorCodes.PARTY_ALREADY_OWNS_UNIT;
    }
}
