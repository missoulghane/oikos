package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class FinancialAccountTest {

    @Test
    void create_starts_with_a_zero_balance() {
        FinancialAccount account = FinancialAccount.create(FinancialAccountId.newId(), EntityId.newId(), "Caisse",
                FinancialAccountType.CASH, "MAD");
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void applying_an_in_entry_increases_the_balance() {
        FinancialAccount account = FinancialAccount.create(FinancialAccountId.newId(), EntityId.newId(), "Caisse",
                FinancialAccountType.CASH, "MAD");

        FinancialAccount credited = account.applyEntry(FinancialEntryDirection.IN, new BigDecimal("500"));

        assertThat(credited.getBalance()).isEqualByComparingTo("500");
    }

    @Test
    void applying_an_out_entry_decreases_the_balance() {
        FinancialAccount account = FinancialAccount.create(FinancialAccountId.newId(), EntityId.newId(), "Caisse",
                FinancialAccountType.CASH, "MAD").applyEntry(FinancialEntryDirection.IN, new BigDecimal("500"));

        FinancialAccount debited = account.applyEntry(FinancialEntryDirection.OUT, new BigDecimal("120"));

        assertThat(debited.getBalance()).isEqualByComparingTo("380");
    }
}
