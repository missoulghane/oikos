package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.exception.BusinessException;

/** Rouvrir suppose d'avoir fermé : une période déjà ouverte n'a rien à rouvrir. */
public class PeriodNotClosedException extends BusinessException {

    public PeriodNotClosedException(PeriodId id) {
        super("Period " + id + " is already open");
    }
}
