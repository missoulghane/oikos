package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.infrastructure.persistence.AccountingExerciseEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface AccountingExercisePersistenceMapper {

    default AccountingExerciseEntity toEntity(AccountingExercise exercise) {
        return toEntity(exercise, new AccountingExerciseEntity());
    }

    default AccountingExerciseEntity toEntity(AccountingExercise exercise, AccountingExerciseEntity entity) {
        entity.setId(exercise.getId().asUuid());
        entity.setPropertyId(exercise.getPropertyId().value());
        entity.setLabel(exercise.getLabel());
        entity.setStartDate(exercise.getStartDate());
        entity.setEndDate(exercise.getEndDate());
        entity.setStatus(exercise.getStatus());
        entity.setClosedAt(exercise.getClosedAt());
        entity.setClosedByUserId(exercise.getClosedByUserId() == null ? null : exercise.getClosedByUserId().value());
        entity.setComment(exercise.getComment());
        return entity;
    }

    default AccountingExercise toDomain(AccountingExerciseEntity entity) {
        EntityId closedByUserId = entity.getClosedByUserId() == null ? null : EntityId.of(entity.getClosedByUserId());
        return AccountingExercise.reconstruct(AccountingExerciseId.of(entity.getId()), EntityId.of(entity.getPropertyId()),
                entity.getLabel(), entity.getStartDate(), entity.getEndDate(), entity.getStatus(),
                entity.getClosedAt(), closedByUserId, entity.getComment());
    }
}
