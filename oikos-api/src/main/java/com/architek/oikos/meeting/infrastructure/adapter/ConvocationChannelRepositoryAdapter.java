package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.domain.model.ConvocationChannel;
import com.architek.oikos.meeting.domain.repository.ConvocationChannelRepository;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.infrastructure.mapper.ConvocationChannelPersistenceMapper;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationChannelJpaRepository;

@Component
public class ConvocationChannelRepositoryAdapter implements ConvocationChannelRepository {

    private final ConvocationChannelJpaRepository jpaRepository;
    private final ConvocationChannelPersistenceMapper mapper;

    public ConvocationChannelRepositoryAdapter(ConvocationChannelJpaRepository jpaRepository,
                                                ConvocationChannelPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ConvocationChannel> findAllActive() {
        return jpaRepository.findByActiveTrueOrderByPositionAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<ConvocationChannel> findByCode(ChannelCode code) {
        return jpaRepository.findById(code.value()).map(mapper::toDomain);
    }

    @Override
    public List<ConvocationChannel> findAll() {
        return jpaRepository.findAllByOrderByPositionAsc().stream().map(mapper::toDomain).toList();
    }
}
