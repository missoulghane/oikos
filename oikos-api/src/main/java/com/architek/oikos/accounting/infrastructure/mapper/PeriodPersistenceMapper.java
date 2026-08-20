package com.architek.oikos.accounting.infrastructure.mapper;

import java.time.YearMonth;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.domain.valueobject.PeriodStatus;
import com.architek.oikos.accounting.infrastructure.persistence.PeriodEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface PeriodPersistenceMapper {

    default PeriodEntity toEntity(Period period) {
        return toEntity(period, new PeriodEntity());
    }

    default PeriodEntity toEntity(Period period, PeriodEntity entity) {
        entity.setId(period.getId().asUuid());
        entity.setExerciseId(period.getExerciseId().asUuid());
        entity.setYearMonth(period.getYearMonth().atDay(1));
        entity.setStatus(period.getStatus().name());
        entity.setClosedAt(period.getClosedAt());
        entity.setClosedByUserId(period.getClosedByUserId() == null ? null : period.getClosedByUserId().value());
        entity.setReopenedAt(period.getReopenedAt());
        entity.setReopenedByUserId(period.getReopenedByUserId() == null ? null : period.getReopenedByUserId().value());
        return entity;
    }

    default Period toDomain(PeriodEntity entity) {
        return Period.reconstruct(PeriodId.of(entity.getId()), AccountingExerciseId.of(entity.getExerciseId()),
                YearMonth.from(entity.getYearMonth()), PeriodStatus.valueOf(entity.getStatus()), entity.getClosedAt(),
                entity.getClosedByUserId() == null ? null : EntityId.of(entity.getClosedByUserId()),
                entity.getReopenedAt(),
                entity.getReopenedByUserId() == null ? null : EntityId.of(entity.getReopenedByUserId()));
    }
}
