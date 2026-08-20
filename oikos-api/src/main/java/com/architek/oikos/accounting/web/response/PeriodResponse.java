package com.architek.oikos.accounting.web.response;

import java.time.Instant;

import com.architek.oikos.accounting.application.dto.PeriodView;
import com.architek.oikos.accounting.domain.valueobject.PeriodStatus;

public record PeriodResponse(String id, String exerciseId, String yearMonth, PeriodStatus status, Instant closedAt,
                              String closedByUserId, Instant reopenedAt) {

    public static PeriodResponse from(PeriodView view) {
        return new PeriodResponse(view.id().toString(), view.exerciseId().toString(), view.yearMonth().toString(),
                view.status(), view.closedAt(), view.closedByUserId() == null ? null : view.closedByUserId().toString(),
                view.reopenedAt());
    }
}
