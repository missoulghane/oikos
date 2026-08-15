package com.architek.oikos.installment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.installment.domain.valueobject.ReceiptNumber;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ReceiptNumberSequenceAdapter.class)
class ReceiptNumberSequenceAdapterDataJpaTest {

    @Autowired
    private ReceiptNumberSequenceAdapter adapter;

    private static EntityId newProperty() {
        return EntityId.of(UUID.randomUUID());
    }

    @Test
    void starts_a_new_series_at_one() {
        assertThat(adapter.allocate(newProperty(), 2026)).isEqualTo(new ReceiptNumber(2026, 1));
    }

    @Test
    void never_hands_out_the_same_number_twice() {
        EntityId property = newProperty();

        assertThat(adapter.allocate(property, 2026).sequence()).isEqualTo(1);
        assertThat(adapter.allocate(property, 2026).sequence()).isEqualTo(2);
        assertThat(adapter.allocate(property, 2026).sequence()).isEqualTo(3);
    }

    // Each copropriété keeps its own series - that is what makes REC-2026-0001
    // meaningful on a receipt that names its copropriété.
    @Test
    void keeps_one_series_per_property() {
        EntityId first = newProperty();
        EntityId second = newProperty();

        adapter.allocate(first, 2026);
        adapter.allocate(first, 2026);

        assertThat(adapter.allocate(second, 2026).sequence())
                .as("a second property must start its own series, not continue the first's")
                .isEqualTo(1);
    }

    @Test
    void restarts_the_series_each_year() {
        EntityId property = newProperty();

        adapter.allocate(property, 2025);
        adapter.allocate(property, 2025);

        assertThat(adapter.allocate(property, 2026)).isEqualTo(new ReceiptNumber(2026, 1));
    }
}
