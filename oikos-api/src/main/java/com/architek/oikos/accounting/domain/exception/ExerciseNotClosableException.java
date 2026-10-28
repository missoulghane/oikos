package com.architek.oikos.accounting.domain.exception;

import java.util.List;

import com.architek.oikos.shared.exception.ConflictException;

/**
 * Spec &sect;12 (EXERCICE_NON_CLOTURABLE): one or more blocking checks failed
 * (ExerciseClosingValidator). Every failing check is listed in the message,
 * rather than stopping at the first one.
 */
public class ExerciseNotClosableException extends ConflictException {

    public ExerciseNotClosableException(List<String> violations) {
        super("L'exercice ne peut pas être clôturé : " + String.join(", ", violations));
    }
}
