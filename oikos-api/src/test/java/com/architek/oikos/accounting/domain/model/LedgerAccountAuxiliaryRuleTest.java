package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.exception.CollectiveAccountRequiresAuxiliaryException;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class LedgerAccountAuxiliaryRuleTest {

    private JournalEntryLine lineWithAuxiliary(LedgerAccountId accountId, EntityId unitId) {
        return JournalEntryLine.of(JournalEntryLineId.newId(), accountId, unitId, null, EntryDirection.CREDIT,
                Amount.of(new BigDecimal("100.00")), "Test line");
    }

    private JournalEntryLine lineWithoutAuxiliary(LedgerAccountId accountId) {
        return JournalEntryLine.of(JournalEntryLineId.newId(), accountId, null, null, EntryDirection.CREDIT,
                Amount.of(new BigDecimal("100.00")), "Test line");
    }

    @Test
    void I6_a_collective_account_without_auxiliary_is_rejected() {
        LedgerAccountId accountId = LedgerAccountId.newId();
        LedgerAccount collective = LedgerAccount.create(accountId, null, null, AccountNumber.of("44150000"),
                "Avances coproprietaires", 4, AccountNature.BALANCE_LIABILITY, true, AccountRole.UNIT_ADVANCE);

        assertThatThrownBy(() -> LedgerAccountAuxiliaryRule.validate(collective, lineWithoutAuxiliary(accountId)))
                .isInstanceOf(CollectiveAccountRequiresAuxiliaryException.class);
    }

    @Test
    void I6_a_collective_account_with_auxiliary_is_accepted() {
        LedgerAccountId accountId = LedgerAccountId.newId();
        LedgerAccount collective = LedgerAccount.create(accountId, null, null, AccountNumber.of("44150000"),
                "Avances coproprietaires", 4, AccountNature.BALANCE_LIABILITY, true, AccountRole.UNIT_ADVANCE);

        assertThatCode(() -> LedgerAccountAuxiliaryRule.validate(collective, lineWithAuxiliary(accountId, EntityId.newId())))
                .doesNotThrowAnyException();
    }

    @Test
    void a_directly_postable_account_without_auxiliary_is_accepted() {
        LedgerAccountId accountId = LedgerAccountId.newId();
        LedgerAccount direct = LedgerAccount.create(accountId, EntityId.newId(), EntityId.newId(),
                AccountNumber.of("34115001"), "Creance lot 1", 3, AccountNature.BALANCE_ASSET, false,
                AccountRole.UNIT_RECEIVABLE);

        assertThatCode(() -> LedgerAccountAuxiliaryRule.validate(direct, lineWithoutAuxiliary(accountId)))
                .doesNotThrowAnyException();
    }
}
