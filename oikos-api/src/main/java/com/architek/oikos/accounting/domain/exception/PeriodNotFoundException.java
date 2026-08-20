package com.architek.oikos.accounting.domain.exception;

import java.time.YearMonth;

import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class PeriodNotFoundException extends ResourceNotFoundException {

    public PeriodNotFoundException(YearMonth period) {
        super("Aucune période " + period + " dans l'exercice ouvert");
    }
}
