package com.architek.oikos.property.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.infrastructure.mapper.UnitTypeDefinitionPersistenceMapperImpl;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UnitTypeDefinitionRepositoryAdapter.class, UnitTypeDefinitionPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class UnitTypeDefinitionRepositoryAdapterDataJpaTest {

    @Autowired
    private UnitTypeDefinitionRepositoryAdapter adapter;

    @Test
    void saves_and_finds_a_unit_type_by_id() {
        PropertyId propertyId = PropertyId.newId();
        UnitTypeDefinition unitType = UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), propertyId, "Appartement");

        adapter.save(unitType);

        assertThat(adapter.findById(unitType.getId())).isPresent()
                .get().extracting(UnitTypeDefinition::getName).isEqualTo("Appartement");
    }

    @Test
    void findByPropertyIdAndName_finds_a_matching_entry() {
        PropertyId propertyId = PropertyId.newId();
        adapter.save(UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), propertyId, "Appartement"));

        assertThat(adapter.findByPropertyIdAndName(propertyId, "Appartement")).isPresent();
        assertThat(adapter.findByPropertyIdAndName(propertyId, "Box")).isEmpty();
    }

    @Test
    void findAllByPropertyId_returns_only_entries_for_that_property() {
        PropertyId propertyId = PropertyId.newId();
        adapter.save(UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), propertyId, "Appartement"));
        adapter.save(UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), PropertyId.newId(), "Box"));

        assertThat(adapter.findAllByPropertyId(propertyId)).hasSize(1);
    }

    @Test
    void existsByPropertyIdAndName_reflects_persisted_state() {
        PropertyId propertyId = PropertyId.newId();
        assertThat(adapter.existsByPropertyIdAndName(propertyId, "Appartement")).isFalse();

        adapter.save(UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), propertyId, "Appartement"));

        assertThat(adapter.existsByPropertyIdAndName(propertyId, "Appartement")).isTrue();
    }

    @Test
    void deleteById_removes_the_entry() {
        UnitTypeDefinition unitType = UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), PropertyId.newId(), "Box");
        adapter.save(unitType);

        adapter.deleteById(unitType.getId());

        assertThat(adapter.findById(unitType.getId())).isEmpty();
    }
}
