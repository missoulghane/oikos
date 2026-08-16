package com.architek.oikos.meeting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.infrastructure.persistence.GeneralMeetingEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface GeneralMeetingPersistenceMapper {

    default GeneralMeetingEntity toEntity(GeneralMeeting meeting) {
        return toEntity(meeting, new GeneralMeetingEntity());
    }

    default GeneralMeetingEntity toEntity(GeneralMeeting meeting, GeneralMeetingEntity entity) {
        entity.setId(meeting.getId().asUuid());
        entity.setPropertyId(meeting.getPropertyId().value());
        entity.setMeetingType(meeting.getMeetingType());
        entity.setStatus(meeting.getStatus());
        entity.setTitle(meeting.getTitle());
        entity.setScheduledAt(meeting.getScheduledAt());
        MeetingVenue venue = meeting.getVenue();
        entity.setVenueType(venue != null ? venue.type() : null);
        entity.setVenueAddress(venue != null ? venue.address() : null);
        entity.setVenueLink(venue != null ? venue.link() : null);
        entity.setQuorumPercentage(meeting.getQuorumPercentage().value());
        entity.setVotingWeightMode(meeting.getVotingWeightMode());
        entity.setComment(meeting.getComment());
        entity.setPublicReference(meeting.getPublicReference().value());
        entity.setOpenedWithoutQuorum(meeting.isOpenedWithoutQuorum());
        return entity;
    }

    default GeneralMeeting toDomain(GeneralMeetingEntity entity) {
        MeetingVenue venue = entity.getVenueType() == null ? null
                : new MeetingVenue(entity.getVenueType(), entity.getVenueAddress(), entity.getVenueLink());
        return GeneralMeeting.reconstruct(GeneralMeetingId.of(entity.getId()), EntityId.of(entity.getPropertyId()),
                entity.getMeetingType(), entity.getStatus(), entity.getTitle(), entity.getScheduledAt(), venue,
                QuorumPercentage.of(entity.getQuorumPercentage()), entity.getVotingWeightMode(),
                entity.getComment(), ShortCode.of(entity.getPublicReference()), entity.isOpenedWithoutQuorum(),
                entity.getCreatedDate());
    }
}
