package com.architek.oikos.accounting.infrastructure.adapter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.infrastructure.mapper.AccountingExercisePersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.AccountingExerciseEntity;
import com.architek.oikos.accounting.infrastructure.persistence.AccountingExerciseJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class AccountingExerciseRepositoryAdapter implements AccountingExerciseRepository {

    private final AccountingExerciseJpaRepository jpaRepository;
    private final AccountingExercisePersistenceMapper mapper;

    public AccountingExerciseRepositoryAdapter(AccountingExerciseJpaRepository jpaRepository,
                                                AccountingExercisePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public AccountingExercise save(AccountingExercise exercise) {
        AccountingExerciseEntity entity = jpaRepository.findById(exercise.getId().asUuid())
                .orElseGet(AccountingExerciseEntity::new);
        mapper.toEntity(exercise, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<AccountingExercise> findById(AccountingExerciseId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<AccountingExercise> findAllByPropertyId(EntityId propertyId) {
        return jpaRepository.findAllByPropertyIdOrderByStartDateAsc(propertyId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<AccountingExercise> findCoveringDate(EntityId propertyId, LocalDate date) {
        return jpaRepository.findCoveringDate(propertyId.value(), date).map(mapper::toDomain);
    }
}
