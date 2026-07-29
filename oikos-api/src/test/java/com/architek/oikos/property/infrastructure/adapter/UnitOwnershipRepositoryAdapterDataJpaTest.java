package com.architek.oikos.property.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.property.domain.model.UnitOwnership;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.OwnershipShare;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.property.infrastructure.mapper.UnitOwnershipPersistenceMapperImpl;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UnitOwnershipRepositoryAdapter.class, UnitOwnershipPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class UnitOwnershipRepositoryAdapterDataJpaTest {

    @Autowired
    private UnitOwnershipRepositoryAdapter adapter;

    @Test
    void saves_and_finds_a_unit_ownership_by_id() {
        UnitId unitId = UnitId.newId();
        EntityId partyId = EntityId.newId();
        UnitOwnership unitOwnership = UnitOwnership.create(UnitOwnershipId.newId(), unitId, partyId, PropertyId.newId(),
                OwnershipShare.of(new BigDecimal("50")));

        adapter.save(unitOwnership);

        assertThat(adapter.findById(unitOwnership.getId())).isPresent()
                .get().extracting(pl -> pl.getOwnershipShare().value()).satisfies(value ->
                        assertThat(value).isEqualByComparingTo("50"));
    }

    @Test
    void findAllByUnitId_returns_only_entries_for_that_unit() {
        UnitId unitId = UnitId.newId();
        adapter.save(UnitOwnership.create(UnitOwnershipId.newId(), unitId, EntityId.newId(), PropertyId.newId(),
                OwnershipShare.of(BigDecimal.TEN)));
        adapter.save(UnitOwnership.create(UnitOwnershipId.newId(), UnitId.newId(), EntityId.newId(), PropertyId.newId(),
                OwnershipShare.of(BigDecimal.ONE)));

        assertThat(adapter.findAllByUnitId(unitId)).hasSize(1);
    }

    @Test
    void existsByUnitIdAndPartyId_reflects_persisted_state() {
        UnitId unitId = UnitId.newId();
        EntityId partyId = EntityId.newId();
        assertThat(adapter.existsByUnitIdAndPartyId(unitId, partyId)).isFalse();

        adapter.save(UnitOwnership.create(UnitOwnershipId.newId(), unitId, partyId, PropertyId.newId(),
                OwnershipShare.of(BigDecimal.TEN)));

        assertThat(adapter.existsByUnitIdAndPartyId(unitId, partyId)).isTrue();
    }

    @Test
    void deleteById_removes_the_entry() {
        UnitOwnership unitOwnership = UnitOwnership.create(UnitOwnershipId.newId(), UnitId.newId(), EntityId.newId(),
                PropertyId.newId(), OwnershipShare.of(BigDecimal.TEN));
        adapter.save(unitOwnership);

        adapter.deleteById(unitOwnership.getId());

        assertThat(adapter.findById(unitOwnership.getId())).isEmpty();
    }
}
