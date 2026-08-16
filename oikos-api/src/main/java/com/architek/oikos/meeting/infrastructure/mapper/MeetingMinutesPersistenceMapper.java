package com.architek.oikos.meeting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.meeting.domain.model.MeetingMinutes;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingMinutesId;
import com.architek.oikos.meeting.infrastructure.persistence.MeetingMinutesEntity;

@Mapper(componentModel = "spring")
public interface MeetingMinutesPersistenceMapper {

    default MeetingMinutesEntity toEntity(MeetingMinutes minutes) {
        return toEntity(minutes, new MeetingMinutesEntity());
    }

    default MeetingMinutesEntity toEntity(MeetingMinutes minutes, MeetingMinutesEntity entity) {
        entity.setId(minutes.getId().asUuid());
        entity.setGeneralMeetingId(minutes.getGeneralMeetingId().asUuid());
        entity.setContent(minutes.getContent());
        entity.setStatus(minutes.getStatus());
        entity.setPublishedAt(minutes.getPublishedAt());
        return entity;
    }

    default MeetingMinutes toDomain(MeetingMinutesEntity entity) {
        return MeetingMinutes.reconstruct(MeetingMinutesId.of(entity.getId()),
                GeneralMeetingId.of(entity.getGeneralMeetingId()), entity.getContent(), entity.getStatus(),
                entity.getPublishedAt(), entity.getCreatedDate());
    }
}
