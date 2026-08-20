package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

/**
 * Ce que la clôture a produit : l'exercice scellé, le résultat arrêté, et
 * l'écriture qui a soldé les classes 6 et 7 - absente quand il n'y avait rien à
 * solder (un exercice sans aucun produit ni charge se clôture quand même).
 */
public record ExerciseClosingView(AccountingExerciseView exercise, BigDecimal netResult, JournalEntryId closingEntryId) {
}
