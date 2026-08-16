package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.domain.model.MeetingQuorumSetting;
import com.architek.oikos.meeting.domain.repository.MeetingQuorumSettingRepository;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.infrastructure.mapper.MeetingQuorumSettingPersistenceMapper;
import com.architek.oikos.meeting.infrastructure.persistence.MeetingQuorumSettingEntity;
import com.architek.oikos.meeting.infrastructure.persistence.MeetingQuorumSettingJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class MeetingQuorumSettingRepositoryAdapter implements MeetingQuorumSettingRepository {

    private final MeetingQuorumSettingJpaRepository jpaRepository;
    private final MeetingQuorumSettingPersistenceMapper mapper;

    public MeetingQuorumSettingRepositoryAdapter(MeetingQuorumSettingJpaRepository jpaRepository,
                                                  MeetingQuorumSettingPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public MeetingQuorumSetting save(MeetingQuorumSetting setting) {
        MeetingQuorumSettingEntity entity = jpaRepository.findById(setting.getId().asUuid())
                .orElseGet(MeetingQuorumSettingEntity::new);
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(setting, entity)));
    }

    @Override
    public Optional<MeetingQuorumSetting> findByPropertyAndType(EntityId propertyId, MeetingType meetingType) {
        return jpaRepository.findByPropertyIdAndMeetingType(propertyId.value(), meetingType).map(mapper::toDomain);
    }

    @Override
    public List<MeetingQuorumSetting> findByProperty(EntityId propertyId) {
        return jpaRepository.findByPropertyId(propertyId.value()).stream().map(mapper::toDomain).toList();
    }
}
