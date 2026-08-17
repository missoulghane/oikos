package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.domain.model.ReplyMedium;
import com.architek.oikos.meeting.domain.repository.ReplyMediumRepository;
import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;
import com.architek.oikos.meeting.infrastructure.mapper.ReplyMediumPersistenceMapper;
import com.architek.oikos.meeting.infrastructure.persistence.ReplyMediumJpaRepository;

@Component
public class ReplyMediumRepositoryAdapter implements ReplyMediumRepository {

    private final ReplyMediumJpaRepository jpaRepository;
    private final ReplyMediumPersistenceMapper mapper;

    public ReplyMediumRepositoryAdapter(ReplyMediumJpaRepository jpaRepository,
                                         ReplyMediumPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ReplyMedium> findAllActive() {
        return jpaRepository.findByActiveTrueOrderByPositionAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<ReplyMedium> findByCode(ReplyMediumCode code) {
        return jpaRepository.findById(code.value()).map(mapper::toDomain);
    }

    @Override
    public List<ReplyMedium> findAll() {
        return jpaRepository.findAllByOrderByPositionAsc().stream().map(mapper::toDomain).toList();
    }
}
