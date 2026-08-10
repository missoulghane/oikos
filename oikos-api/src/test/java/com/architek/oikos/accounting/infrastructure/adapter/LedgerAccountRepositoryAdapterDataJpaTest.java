package com.architek.oikos.accounting.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.infrastructure.mapper.LedgerAccountPersistenceMapperImpl;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({LedgerAccountRepositoryAdapter.class, LedgerAccountPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class LedgerAccountRepositoryAdapterDataJpaTest {

    @Autowired
    private LedgerAccountRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_a_unit_scoped_account() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        LedgerAccount saved = adapter.save(LedgerAccount.create(LedgerAccountId.newId(), propertyId, unitId,
                AccountNumber.of("34115001"), "Coproprietaire - creance", 3, AccountNature.BALANCE_ASSET, false,
                AccountRole.UNIT_RECEIVABLE));

        LedgerAccount reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getPropertyId()).contains(propertyId);
        assertThat(reloaded.getUnitId()).contains(unitId);
        assertThat(reloaded.getAccountNumber().value()).isEqualTo("34115001");
        assertThat(reloaded.getRole()).contains(AccountRole.UNIT_RECEIVABLE);
        assertThat(reloaded.isActive()).isTrue();
    }

    @Test
    void finds_a_property_scoped_singleton_account_by_role() {
        EntityId propertyId = EntityId.newId();
        adapter.save(LedgerAccount.create(LedgerAccountId.newId(), propertyId, null, AccountNumber.of("44150000"),
                "Avances", 4, AccountNature.BALANCE_LIABILITY, true, AccountRole.UNIT_ADVANCE));

        LedgerAccount found = adapter.findByPropertyIdAndRole(propertyId, AccountRole.UNIT_ADVANCE).orElseThrow();

        assertThat(found.getAccountNumber().value()).isEqualTo("44150000");
    }

    @Test
    void finds_a_unit_s_own_dedicated_account_by_role() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        adapter.save(LedgerAccount.create(LedgerAccountId.newId(), propertyId, unitId, AccountNumber.of("34115007"),
                "Coproprietaire - creance", 3, AccountNature.BALANCE_ASSET, false, AccountRole.UNIT_RECEIVABLE));

        LedgerAccount found = adapter.findByPropertyIdAndUnitIdAndRole(propertyId, unitId, AccountRole.UNIT_RECEIVABLE)
                .orElseThrow();

        assertThat(found.getAccountNumber().value()).isEqualTo("34115007");
    }

    @Test
    void lists_global_and_property_scoped_accounts_visible_to_a_property_but_not_another_property_s_own_accounts() {
        EntityId propertyId = EntityId.newId();
        EntityId otherPropertyId = EntityId.newId();
        adapter.save(LedgerAccount.create(LedgerAccountId.newId(), null, null, AccountNumber.of("71810000"),
                "Cotisations", 7, AccountNature.INCOME, false, AccountRole.DUES_INCOME));
        adapter.save(LedgerAccount.create(LedgerAccountId.newId(), propertyId, null, AccountNumber.of("51610001"),
                "Caisse", 5, AccountNature.BALANCE_ASSET, false, AccountRole.CASH));
        adapter.save(LedgerAccount.create(LedgerAccountId.newId(), otherPropertyId, null, AccountNumber.of("51610001"),
                "Caisse", 5, AccountNature.BALANCE_ASSET, false, AccountRole.CASH));

        var visible = adapter.findAllVisibleToProperty(propertyId);

        assertThat(visible).extracting(a -> a.getAccountNumber().value())
                .containsExactlyInAnyOrder("71810000", "51610001");
        assertThat(visible).extracting(LedgerAccount::getPropertyId)
                .noneMatch(p -> p.isPresent() && p.get().equals(otherPropertyId));
    }
}
