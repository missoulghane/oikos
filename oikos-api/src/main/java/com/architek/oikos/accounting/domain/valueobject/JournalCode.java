package com.architek.oikos.accounting.domain.valueobject;

import java.util.Optional;

/**
 * The 6 journals of spec &sect;3.3. A fixed, engine-defined set (no CRUD in
 * the API surface - GET /referentiel/journaux is read-only), so modelled as
 * an enum rather than a repository-backed entity; the DB `journal` table
 * (V11) exists as read-only reference data / FK integrity target, not as
 * the source of truth for this metadata.
 */
public enum JournalCode {
    VT("Ventes / Appels de fonds", JournalType.SALES, null, true),
    BQ("Banque", JournalType.TREASURY, AccountRole.BANK, true),
    CA("Caisse", JournalType.TREASURY, AccountRole.CASH, true),
    AC("Achats", JournalType.PURCHASES, null, true),
    OD("Operations diverses", JournalType.MISCELLANEOUS, null, true),
    AN("A-nouveaux", JournalType.OPENING, null, false);

    private final String label;
    private final JournalType type;
    private final AccountRole treasuryRole;
    private final boolean postable;

    JournalCode(String label, JournalType type, AccountRole treasuryRole, boolean postable) {
        this.label = label;
        this.type = type;
        this.treasuryRole = treasuryRole;
        this.postable = postable;
    }

    public String label() {
        return label;
    }

    public JournalType type() {
        return type;
    }

    /** Empty for non-treasury journals; BANK for BQ, CASH for CA. */
    public Optional<AccountRole> treasuryRole() {
        return Optional.ofNullable(treasuryRole);
    }

    /** False only for AN - reserved to the engine's closing use case (spec &sect;3.3), never user-saisissable. */
    public boolean postable() {
        return postable;
    }
}
