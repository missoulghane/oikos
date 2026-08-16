package com.architek.oikos.meeting.infrastructure.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.ConvocationDelivery;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationDeliveryId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.domain.valueobject.VotingWeight;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationDeliveryEntity;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The deliveries live in their own table and are mapped here, but they are
 * passed in and out rather than read off the entity: ConvocationEntity holds no
 * association to them (see its javadoc), so the adapter is what puts the two
 * halves together.
 */
@Mapper(componentModel = "spring")
public interface ConvocationPersistenceMapper {

    default ConvocationEntity toEntity(Convocation convocation) {
        return toEntity(convocation, new ConvocationEntity());
    }

    default ConvocationEntity toEntity(Convocation convocation, ConvocationEntity entity) {
        entity.setId(convocation.getId().asUuid());
        entity.setGeneralMeetingId(convocation.getGeneralMeetingId().asUuid());
        entity.setUnitId(convocation.getUnitId().value());
        entity.setVotingWeight(convocation.getVotingWeight().value());
        entity.setConfirmationToken(convocation.getConfirmationToken());
        entity.setConfirmationCode(convocation.getConfirmationCode().value());
        entity.setAttendanceReply(convocation.getAttendanceReply());
        entity.setRepliedAt(convocation.getRepliedAt());
        entity.setReplySource(convocation.getReplySource());
        entity.setRepliedByPartyId(convocation.getRepliedByPartyId() != null
                ? convocation.getRepliedByPartyId().value() : null);
        entity.setReplyNote(convocation.getReplyNote());
        entity.setCheckedIn(convocation.isCheckedIn());
        entity.setAttendanceMode(convocation.getAttendanceMode());
        entity.setCheckedInPartyId(convocation.getCheckedInPartyId() != null
                ? convocation.getCheckedInPartyId().value() : null);
        entity.setCheckedInAt(convocation.getCheckedInAt());
        return entity;
    }

    default ConvocationDeliveryEntity toEntity(ConvocationDelivery delivery, ConvocationId convocationId) {
        ConvocationDeliveryEntity entity = new ConvocationDeliveryEntity();
        entity.setId(delivery.getId().asUuid());
        entity.setConvocationId(convocationId.asUuid());
        entity.setChannelCode(delivery.getChannelCode().value());
        entity.setStatus(delivery.getStatus());
        entity.setSentAt(delivery.getSentAt());
        entity.setReference(delivery.getReference());
        entity.setRecordedByUserId(delivery.getRecordedByUserId() != null
                ? delivery.getRecordedByUserId().value() : null);
        return entity;
    }

    default ConvocationDelivery toDomain(ConvocationDeliveryEntity entity) {
        return ConvocationDelivery.reconstruct(ConvocationDeliveryId.of(entity.getId()),
                ChannelCode.of(entity.getChannelCode()), entity.getStatus(), entity.getSentAt(), entity.getReference(),
                entity.getRecordedByUserId() != null ? EntityId.of(entity.getRecordedByUserId()) : null,
                entity.getCreatedDate());
    }

    default Convocation toDomain(ConvocationEntity entity, List<ConvocationDelivery> deliveries) {
        return Convocation.reconstruct(ConvocationId.of(entity.getId()),
                GeneralMeetingId.of(entity.getGeneralMeetingId()), EntityId.of(entity.getUnitId()),
                VotingWeight.of(entity.getVotingWeight()), deliveries, entity.getConfirmationToken(),
                ShortCode.of(entity.getConfirmationCode()), entity.getAttendanceReply(),
                entity.getRepliedAt(), entity.getReplySource(),
                entity.getRepliedByPartyId() != null ? EntityId.of(entity.getRepliedByPartyId()) : null,
                entity.getReplyNote(), entity.isCheckedIn(), entity.getAttendanceMode(),
                entity.getCheckedInPartyId() != null ? EntityId.of(entity.getCheckedInPartyId()) : null,
                entity.getCheckedInAt(), entity.getCreatedDate());
    }
}
