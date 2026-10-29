package com.architek.oikos.accounting.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AccountingExerciseRepositoryAdapter.class, com.architek.oikos.accounting.infrastructure.mapper.AccountingExercisePersistenceMapperImpl.class,
        JpaAuditingConfiguration.class})
class AccountingExerciseRepositoryAdapterDataJpaTest {

    @Autowired
    private AccountingExerciseRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_an_exercise() {
        EntityId propertyId = EntityId.newId();
        AccountingExercise saved = adapter.save(AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "Premier exercice"));

        AccountingExercise reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getPropertyId()).isEqualTo(propertyId);
        assertThat(reloaded.getLabel()).isEqualTo("Exercice 2026");
        assertThat(reloaded.isOpen()).isTrue();
    }

    @Test
    void lists_the_exercises_of_a_property_oldest_first() {
        EntityId propertyId = EntityId.newId();
        adapter.save(AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2027",
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), null));
        adapter.save(AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null));
        adapter.save(AccountingExercise.open(AccountingExerciseId.newId(), EntityId.newId(), "Ailleurs",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null));

        assertThat(adapter.findAllByPropertyId(propertyId)).extracting(AccountingExercise::getLabel)
                .containsExactly("Exercice 2026", "Exercice 2027");
    }

    @Test
    void finds_the_exercise_covering_a_date_bounds_included() {
        EntityId propertyId = EntityId.newId();
        adapter.save(AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null));
        adapter.save(AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2027",
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), null));

        assertThat(adapter.findCoveringDate(propertyId, LocalDate.of(2026, 12, 31))).get()
                .extracting(AccountingExercise::getLabel).isEqualTo("Exercice 2026");
        assertThat(adapter.findCoveringDate(propertyId, LocalDate.of(2027, 1, 1))).get()
                .extracting(AccountingExercise::getLabel).isEqualTo("Exercice 2027");
        assertThat(adapter.findCoveringDate(propertyId, LocalDate.of(2028, 1, 1))).isEmpty();
    }
}
