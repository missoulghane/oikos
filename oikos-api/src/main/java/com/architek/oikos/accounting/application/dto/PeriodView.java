package com.architek.oikos.accounting.application.dto;

import java.time.Instant;
import java.time.YearMonth;

import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.domain.valueobject.PeriodStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** {@code reopenedAt} n'est pas effacé par une nouvelle clôture : c'est l'historique du mois, pas son état. */
public record PeriodView(PeriodId id, AccountingExerciseId exerciseId, YearMonth yearMonth, PeriodStatus status,
                          Instant closedAt, EntityId closedByUserId, Instant reopenedAt) {

    public static PeriodView from(Period period) {
        return new PeriodView(period.getId(), period.getExerciseId(), period.getYearMonth(), period.getStatus(),
                period.getClosedAt(), period.getClosedByUserId(), period.getReopenedAt());
    }
}
