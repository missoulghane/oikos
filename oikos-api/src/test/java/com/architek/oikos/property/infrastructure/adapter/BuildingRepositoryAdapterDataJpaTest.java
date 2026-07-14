package com.architek.oikos.property.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.infrastructure.mapper.BuildingPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BuildingRepositoryAdapter.class, BuildingPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class BuildingRepositoryAdapterDataJpaTest {

    @Autowired
    private BuildingRepositoryAdapter adapter;

    @Test
    void saves_and_finds_an_building_by_id() {
        Building building = Building.create(BuildingId.newId(), PropertyId.newId(), "Batiment A", 5);

        adapter.save(building);

        assertThat(adapter.findById(building.getId())).isPresent()
                .get().extracting(Building::getName).isEqualTo("Batiment A");
    }

    @Test
    void findAllByPropertyId_paginates_and_filters_by_parent() {
        PropertyId propertyId = PropertyId.newId();
        for (int i = 0; i < 3; i++) {
            adapter.save(Building.create(BuildingId.newId(), propertyId, "Batiment " + i, i));
        }
        adapter.save(Building.create(BuildingId.newId(), PropertyId.newId(), "Autre copro", 1));

        var page = adapter.findAllByPropertyId(propertyId, PageRequest.of(0, 2));

        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.content()).hasSize(2);
    }
}
