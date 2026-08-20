package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;

/**
 * Le solde net d'un compte <em>pour une copropriété donnée</em>, crédits moins
 * débits, sur les écritures comptabilisées d'un exercice.
 *
 * <p>Il ne se lit pas dans {@code LedgerAccount.balance} : une partie du plan de
 * comptes est partagée entre toutes les copropriétés (les produits d'appels de
 * fonds, les avances, les dettes de personnel), et ce solde-là les totalise
 * toutes. Solder les classes 6 et 7 d'une copropriété à partir de ce total
 * effacerait la part des autres.
 */
public record AccountNetAmount(LedgerAccountId accountId, BigDecimal creditMinusDebit) {
}
