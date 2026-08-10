package com.architek.oikos.accounting.application.dto;

import java.time.Instant;
import java.time.YearMonth;

import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.domain.valueobject.PeriodStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record PeriodView(PeriodId id, AccountingExerciseId exerciseId, YearMonth yearMonth, PeriodStatus status,
                          Instant closedAt, EntityId closedByUserId) {

    public static PeriodView from(Period period) {
        return new PeriodView(period.getId(), period.getExerciseId(), period.getYearMonth(), period.getStatus(),
                period.getClosedAt(), period.getClosedByUserId());
    }
}
