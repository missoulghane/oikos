package com.architek.oikos.accounting.infrastructure.adapter;

import java.time.YearMonth;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.infrastructure.mapper.PeriodPersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.PeriodEntity;
import com.architek.oikos.accounting.infrastructure.persistence.PeriodJpaRepository;

@Component
public class PeriodRepositoryAdapter implements PeriodRepository {

    private final PeriodJpaRepository jpaRepository;
    private final PeriodPersistenceMapper mapper;

    public PeriodRepositoryAdapter(PeriodJpaRepository jpaRepository, PeriodPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Period save(Period period) {
        PeriodEntity entity = jpaRepository.findById(period.getId().asUuid()).orElseGet(PeriodEntity::new);
        mapper.toEntity(period, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Period> findById(PeriodId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Optional<Period> findByExerciseIdAndYearMonth(AccountingExerciseId exerciseId, YearMonth yearMonth) {
        return jpaRepository.findByExerciseIdAndYearMonth(exerciseId.asUuid(), yearMonth.atDay(1)).map(mapper::toDomain);
    }
}
