package com.architek.oikos.accounting.domain.exception;

import java.time.YearMonth;
import java.util.List;

import com.architek.oikos.shared.exception.ConflictException;

/**
 * Un exercice ne se clôture qu'une fois tous ses mois clos : le résultat se
 * calcule sur des périodes arrêtées, pas sur des mois encore ouverts à la
 * saisie.
 */
public class ExerciseNotClosableException extends ConflictException {

    public ExerciseNotClosableException(List<YearMonth> openPeriods) {
        super("L'exercice ne peut pas être clôturé : "
                + openPeriods.stream().map(YearMonth::toString).reduce((a, b) -> a + ", " + b).orElse("")
                + (openPeriods.size() > 1 ? " sont encore ouvertes." : " est encore ouverte."));
    }
}
