package com.architek.oikos.accounting.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({FinancialAccountRepositoryAdapter.class, com.architek.oikos.accounting.infrastructure.mapper.FinancialAccountPersistenceMapperImpl.class,
        JpaAuditingConfiguration.class})
class FinancialAccountRepositoryAdapterDataJpaTest {

    @Autowired
    private FinancialAccountRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_a_financial_account() {
        EntityId propertyId = EntityId.newId();
        FinancialAccount saved = adapter.save(
                FinancialAccount.create(FinancialAccountId.newId(), propertyId, "Caisse", FinancialAccountType.CASH, "MAD"));

        FinancialAccount reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getPropertyId()).isEqualTo(propertyId);
        assertThat(reloaded.getName()).isEqualTo("Caisse");
        assertThat(reloaded.getBalance()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
    }

    @Test
    void finds_all_accounts_of_a_property() {
        EntityId propertyId = EntityId.newId();
        adapter.save(FinancialAccount.create(FinancialAccountId.newId(), propertyId, "Caisse", FinancialAccountType.CASH, "MAD"));
        adapter.save(FinancialAccount.create(FinancialAccountId.newId(), propertyId, "Banque", FinancialAccountType.BANK, "MAD"));
        adapter.save(FinancialAccount.create(FinancialAccountId.newId(), EntityId.newId(), "Autre copro", FinancialAccountType.CASH, "MAD"));

        assertThat(adapter.findAllByPropertyId(propertyId)).hasSize(2);
    }
}
