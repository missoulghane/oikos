package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A ledger account's net balance for one auxiliary unit (spec: compte
 * collectif + auxiliaire - e.g. how much of the collective "avances et
 * acomptes recus" account belongs to a specific lot). amount is expressed
 * on the account's own normal side (mirrors LedgerAccount.balance), never
 * negative for a well-formed query (callers only ask for accounts/units
 * where it's positive).
 */
public record AuxiliaryUnitBalance(EntityId unitId, BigDecimal amount) {
}
