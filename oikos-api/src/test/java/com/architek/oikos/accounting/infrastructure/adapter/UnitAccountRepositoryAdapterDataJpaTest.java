package com.architek.oikos.accounting.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UnitAccountRepositoryAdapter.class, com.architek.oikos.accounting.infrastructure.mapper.UnitAccountPersistenceMapperImpl.class,
        JpaAuditingConfiguration.class})
class UnitAccountRepositoryAdapterDataJpaTest {

    @Autowired
    private UnitAccountRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_a_unit_account() {
        EntityId unitId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        UnitAccount saved = adapter.save(UnitAccount.create(UnitAccountId.newId(), unitId, propertyId, Instant.now()));

        UnitAccount reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getUnitId()).isEqualTo(unitId);
        assertThat(reloaded.getPropertyId()).isEqualTo(propertyId);
    }

    @Test
    void finds_the_account_of_a_unit_and_reports_existence() {
        EntityId unitId = EntityId.newId();
        adapter.save(UnitAccount.create(UnitAccountId.newId(), unitId, EntityId.newId(), Instant.now()));

        assertThat(adapter.findByUnitId(unitId)).isPresent();
        assertThat(adapter.existsByUnitId(unitId)).isTrue();
        assertThat(adapter.existsByUnitId(EntityId.newId())).isFalse();
    }

    @Test
    void finds_all_accounts_of_a_property() {
        EntityId propertyId = EntityId.newId();
        adapter.save(UnitAccount.create(UnitAccountId.newId(), EntityId.newId(), propertyId, Instant.now()));
        adapter.save(UnitAccount.create(UnitAccountId.newId(), EntityId.newId(), propertyId, Instant.now()));

        assertThat(adapter.findAllByPropertyId(propertyId)).hasSize(2);
    }
}
