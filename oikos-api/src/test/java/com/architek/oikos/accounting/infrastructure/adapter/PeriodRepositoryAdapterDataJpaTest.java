package com.architek.oikos.accounting.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.domain.valueobject.PeriodStatus;
import com.architek.oikos.accounting.infrastructure.mapper.PeriodPersistenceMapperImpl;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PeriodRepositoryAdapter.class, PeriodPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class PeriodRepositoryAdapterDataJpaTest {

    @Autowired
    private PeriodRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_an_open_period() {
        AccountingExerciseId exerciseId = AccountingExerciseId.newId();
        Period saved = adapter.save(Period.open(PeriodId.newId(), exerciseId, YearMonth.of(2026, 8)));

        Period reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getExerciseId()).isEqualTo(exerciseId);
        assertThat(reloaded.getYearMonth()).isEqualTo(YearMonth.of(2026, 8));
        assertThat(reloaded.getStatus()).isEqualTo(PeriodStatus.OPEN);
    }

    @Test
    void finds_a_period_by_exercise_and_year_month() {
        AccountingExerciseId exerciseId = AccountingExerciseId.newId();
        adapter.save(Period.open(PeriodId.newId(), exerciseId, YearMonth.of(2026, 9)));

        Period found = adapter.findByExerciseIdAndYearMonth(exerciseId, YearMonth.of(2026, 9)).orElseThrow();

        assertThat(found.getYearMonth()).isEqualTo(YearMonth.of(2026, 9));
    }

    @Test
    void persists_a_closed_period_with_its_closing_metadata() {
        AccountingExerciseId exerciseId = AccountingExerciseId.newId();
        EntityId closedBy = EntityId.newId();
        Period closed = Period.open(PeriodId.newId(), exerciseId, YearMonth.of(2026, 8)).close(Instant.now(), closedBy);

        Period saved = adapter.save(closed);
        Period reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(PeriodStatus.CLOSED);
        assertThat(reloaded.getClosedByUserId()).isEqualTo(closedBy);
    }
}
