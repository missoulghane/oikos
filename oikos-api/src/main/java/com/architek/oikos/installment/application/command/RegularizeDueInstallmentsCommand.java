package com.architek.oikos.installment.application.command;

import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * @param asOf            the date the sweep runs for: an echeance falling due on
 *                        it is imputed, one falling due after is not (same
 *                        cutoff as a payment's value date).
 * @param createdByUserId author of the entries posted, since no human triggered
 *                        them - see RegularizeDueInstallmentsService.
 */
public record RegularizeDueInstallmentsCommand(LocalDate asOf, EntityId createdByUserId) {
}
