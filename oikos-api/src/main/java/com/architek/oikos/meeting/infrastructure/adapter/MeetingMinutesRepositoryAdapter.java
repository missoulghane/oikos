package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.domain.model.MeetingMinutes;
import com.architek.oikos.meeting.domain.repository.MeetingMinutesRepository;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.infrastructure.mapper.MeetingMinutesPersistenceMapper;
import com.architek.oikos.meeting.infrastructure.persistence.MeetingMinutesEntity;
import com.architek.oikos.meeting.infrastructure.persistence.MeetingMinutesJpaRepository;

@Component
public class MeetingMinutesRepositoryAdapter implements MeetingMinutesRepository {

    private final MeetingMinutesJpaRepository jpaRepository;
    private final MeetingMinutesPersistenceMapper mapper;

    public MeetingMinutesRepositoryAdapter(MeetingMinutesJpaRepository jpaRepository,
                                            MeetingMinutesPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public MeetingMinutes save(MeetingMinutes minutes) {
        MeetingMinutesEntity entity = jpaRepository.findById(minutes.getId().asUuid())
                .orElseGet(MeetingMinutesEntity::new);
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(minutes, entity)));
    }

    @Override
    public Optional<MeetingMinutes> findByGeneralMeetingId(GeneralMeetingId generalMeetingId) {
        return jpaRepository.findByGeneralMeetingId(generalMeetingId.asUuid()).map(mapper::toDomain);
    }
}
