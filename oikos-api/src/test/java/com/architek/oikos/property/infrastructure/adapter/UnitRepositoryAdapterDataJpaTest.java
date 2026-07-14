package com.architek.oikos.property.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitType;
import com.architek.oikos.property.infrastructure.mapper.UnitPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UnitRepositoryAdapter.class, UnitPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class UnitRepositoryAdapterDataJpaTest {

    @Autowired
    private UnitRepositoryAdapter adapter;

    @Test
    void saves_and_finds_a_unit_by_id() {
        Unit unit = Unit.create(UnitId.newId(), BuildingId.newId(), "A12", UnitType.APARTMENT, Shares.of(new BigDecimal("150.00")));

        adapter.save(unit);

        assertThat(adapter.findById(unit.getId())).isPresent()
                .get().extracting(Unit::getUnitNumber).isEqualTo("A12");
    }

    @Test
    void findAllByBuildingId_paginates_and_filters_by_parent() {
        BuildingId buildingId = BuildingId.newId();
        for (int i = 0; i < 3; i++) {
            adapter.save(Unit.create(UnitId.newId(), buildingId, "A" + i, UnitType.APARTMENT, Shares.of(BigDecimal.TEN)));
        }
        adapter.save(Unit.create(UnitId.newId(), BuildingId.newId(), "Z99", UnitType.PARKING, Shares.of(BigDecimal.ONE)));

        var page = adapter.findAllByBuildingId(buildingId, PageRequest.of(0, 2));

        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.content()).hasSize(2);
    }
}
