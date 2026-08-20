package com.architek.oikos.accounting.domain.exception;

import java.time.YearMonth;
import java.util.List;

import com.architek.oikos.shared.exception.ConflictException;

/**
 * Une période close plus récente barre la route : il faut la rouvrir d'abord,
 * sans quoi le préfixe continu de périodes closes serait percé en son milieu.
 */
public class PeriodNotReopenableException extends ConflictException {

    public PeriodNotReopenableException(YearMonth period, List<YearMonth> closedAfter) {
        super("La période " + period + " ne peut pas être rouverte : "
                + closedAfter.stream().map(YearMonth::toString).reduce((a, b) -> a + ", " + b).orElse("")
                + " sont clôturées après elle. Rouvrez-les d'abord, de la plus récente à la plus ancienne.");
    }
}
