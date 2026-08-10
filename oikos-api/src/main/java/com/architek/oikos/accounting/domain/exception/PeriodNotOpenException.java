package com.architek.oikos.accounting.domain.exception;

import java.time.LocalDate;

import com.architek.oikos.shared.exception.ConflictException;

/** I5/I12 (spec &sect;12: PERIODE_CLOTUREE): the piece date must fall in an OPEN period. */
public class PeriodNotOpenException extends ConflictException {

    public PeriodNotOpenException(LocalDate pieceDate) {
        super("No open period covers piece date: " + pieceDate);
    }
}
