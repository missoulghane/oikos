package com.architek.oikos.meeting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.meeting.domain.model.MeetingQuorumSetting;
import com.architek.oikos.meeting.domain.valueobject.MeetingQuorumSettingId;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.meeting.infrastructure.persistence.MeetingQuorumSettingEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface MeetingQuorumSettingPersistenceMapper {

    default MeetingQuorumSettingEntity toEntity(MeetingQuorumSetting setting) {
        return toEntity(setting, new MeetingQuorumSettingEntity());
    }

    default MeetingQuorumSettingEntity toEntity(MeetingQuorumSetting setting, MeetingQuorumSettingEntity entity) {
        entity.setId(setting.getId().asUuid());
        entity.setPropertyId(setting.getPropertyId().value());
        entity.setMeetingType(setting.getMeetingType());
        entity.setQuorumPercentage(setting.getQuorumPercentage().value());
        return entity;
    }

    default MeetingQuorumSetting toDomain(MeetingQuorumSettingEntity entity) {
        return MeetingQuorumSetting.reconstruct(MeetingQuorumSettingId.of(entity.getId()),
                EntityId.of(entity.getPropertyId()), entity.getMeetingType(),
                QuorumPercentage.of(entity.getQuorumPercentage()));
    }
}
