package com.architek.oikos.property.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.infrastructure.mapper.PropertyPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PropertyRepositoryAdapter.class, PropertyPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class PropertyRepositoryAdapterDataJpaTest {

    @Autowired
    private PropertyRepositoryAdapter adapter;

    @Test
    void saves_and_finds_a_property_by_id() {
        Property property = Property.create(PropertyId.newId(), "Copro Laumiere", "33 Avenue de Laumiere");

        adapter.save(property);

        assertThat(adapter.findById(property.getId())).isPresent()
                .get().extracting(Property::getName).isEqualTo("Copro Laumiere");
    }

    @Test
    void findAll_paginates_results() {
        for (int i = 0; i < 3; i++) {
            adapter.save(Property.create(PropertyId.newId(), "Copro " + i, "Address " + i));
        }

        var page = adapter.findAll(PageRequest.of(0, 2));

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(3);
    }
}
