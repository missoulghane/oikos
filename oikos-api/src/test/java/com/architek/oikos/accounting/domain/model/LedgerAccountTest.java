package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class LedgerAccountTest {

    @Test
    void normal_side_is_derived_from_nature_not_stored_independently() {
        LedgerAccount asset = LedgerAccount.create(LedgerAccountId.newId(), null, null, AccountNumber.of("51410001"),
                "Banque", 5, AccountNature.BALANCE_ASSET, false, AccountRole.BANK);
        LedgerAccount income = LedgerAccount.create(LedgerAccountId.newId(), null, null, AccountNumber.of("71810000"),
                "Cotisations", 7, AccountNature.INCOME, false, AccountRole.DUES_INCOME);

        assertThat(asset.getNormalSide()).isEqualTo(EntryDirection.DEBIT);
        assertThat(income.getNormalSide()).isEqualTo(EntryDirection.CREDIT);
    }

    @Test
    void a_unit_scoped_account_must_also_carry_its_property() {
        assertThatThrownBy(() -> LedgerAccount.create(LedgerAccountId.newId(), null, EntityId.newId(),
                AccountNumber.of("34115001"), "Creance lot 1", 3, AccountNature.BALANCE_ASSET, false,
                AccountRole.UNIT_RECEIVABLE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void account_class_must_be_between_1_and_8() {
        assertThatThrownBy(() -> LedgerAccount.create(LedgerAccountId.newId(), null, null, AccountNumber.of("99999999"),
                "Invalide", 9, AccountNature.BALANCE_ASSET, false, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void I6_a_collective_account_requires_an_auxiliary() {
        LedgerAccount collective = LedgerAccount.create(LedgerAccountId.newId(), null, null,
                AccountNumber.of("44150000"), "Avances coproprietaires", 4, AccountNature.BALANCE_LIABILITY, true,
                AccountRole.UNIT_ADVANCE);

        assertThat(collective.requiresAuxiliary()).isTrue();
    }

    @Test
    void a_directly_postable_account_never_requires_an_auxiliary() {
        LedgerAccount direct = LedgerAccount.create(LedgerAccountId.newId(), EntityId.newId(), EntityId.newId(),
                AccountNumber.of("34115001"), "Creance lot 1", 3, AccountNature.BALANCE_ASSET, false,
                AccountRole.UNIT_RECEIVABLE);

        assertThat(direct.requiresAuxiliary()).isFalse();
    }

    @Test
    void a_newly_created_account_starts_with_a_zero_balance() {
        LedgerAccount account = LedgerAccount.create(LedgerAccountId.newId(), null, null, AccountNumber.of("51610001"),
                "Caisse", 5, AccountNature.BALANCE_ASSET, false, AccountRole.CASH);

        assertThat(account.getBalance()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
    }
}
