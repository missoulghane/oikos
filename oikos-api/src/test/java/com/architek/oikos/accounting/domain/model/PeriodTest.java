package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.exception.PeriodAlreadyClosedException;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.domain.valueobject.PeriodStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class PeriodTest {

    @Test
    void open_creates_an_open_period() {
        Period period = Period.open(PeriodId.newId(), AccountingExerciseId.newId(), YearMonth.of(2026, 8));

        assertThat(period.isOpen()).isTrue();
        assertThat(period.getStatus()).isEqualTo(PeriodStatus.OPEN);
    }

    @Test
    void close_transitions_to_closed() {
        Period period = Period.open(PeriodId.newId(), AccountingExerciseId.newId(), YearMonth.of(2026, 8));

        Period closed = period.close(Instant.now(), EntityId.newId());

        assertThat(closed.isOpen()).isFalse();
        assertThat(closed.getStatus()).isEqualTo(PeriodStatus.CLOSED);
    }

    @Test
    void a_period_cannot_be_closed_twice() {
        Period closed = Period.open(PeriodId.newId(), AccountingExerciseId.newId(), YearMonth.of(2026, 8))
                .close(Instant.now(), EntityId.newId());

        assertThatThrownBy(() -> closed.close(Instant.now(), EntityId.newId()))
                .isInstanceOf(PeriodAlreadyClosedException.class);
    }
}
