package com.architek.oikos.accounting.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.architek.oikos.shared.domain.valueobject.EntityId;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(LedgerAccountNumberSequenceRepositoryAdapter.class)
class LedgerAccountNumberSequenceRepositoryAdapterDataJpaTest {

    @Autowired
    private LedgerAccountNumberSequenceRepositoryAdapter adapter;

    @Test
    void the_first_allocation_for_a_new_property_and_prefix_is_1() {
        int allocated = adapter.allocateNextIncrement(EntityId.newId(), "516100");

        assertThat(allocated).isEqualTo(1);
    }

    @Test
    void successive_allocations_for_the_same_property_and_prefix_increment_by_1() {
        EntityId propertyId = EntityId.newId();

        assertThat(adapter.allocateNextIncrement(propertyId, "341150")).isEqualTo(1);
        assertThat(adapter.allocateNextIncrement(propertyId, "341150")).isEqualTo(2);
        assertThat(adapter.allocateNextIncrement(propertyId, "341150")).isEqualTo(3);
    }

    @Test
    void each_prefix_has_its_own_independent_counter() {
        EntityId propertyId = EntityId.newId();

        assertThat(adapter.allocateNextIncrement(propertyId, "516100")).isEqualTo(1);
        assertThat(adapter.allocateNextIncrement(propertyId, "341150")).isEqualTo(1);
        assertThat(adapter.allocateNextIncrement(propertyId, "516100")).isEqualTo(2);
    }

    @Test
    void each_property_has_its_own_independent_counter_for_the_same_prefix() {
        EntityId propertyA = EntityId.newId();
        EntityId propertyB = EntityId.newId();

        assertThat(adapter.allocateNextIncrement(propertyA, "341150")).isEqualTo(1);
        assertThat(adapter.allocateNextIncrement(propertyB, "341150")).isEqualTo(1);
        assertThat(adapter.allocateNextIncrement(propertyA, "341150")).isEqualTo(2);
    }
}
