package com.architek.oikos.meeting.infrastructure.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.ConvocationDelivery;
import com.architek.oikos.meeting.domain.model.ConvocationReply;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationDeliveryId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationReplyId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.domain.valueobject.VotingWeight;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationDeliveryEntity;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationEntity;
import com.architek.oikos.meeting.infrastructure.persistence.ConvocationReplyEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The deliveries and the replies live in their own tables and are mapped here,
 * but they are passed in and out rather than read off the entity:
 * ConvocationEntity holds no association to them (see its javadoc), so the
 * adapter is what puts the three parts together.
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
        entity.setReplyMedium(convocation.getReplyMedium() != null ? convocation.getReplyMedium().value() : null);
        entity.setReplyAttendanceMode(convocation.getReplyAttendanceMode());
        entity.setReplyByProxy(convocation.isReplyByProxy());
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
        entity.setReminder(delivery.isReminder());
        entity.setRecordedByUserId(delivery.getRecordedByUserId() != null
                ? delivery.getRecordedByUserId().value() : null);
        return entity;
    }

    default ConvocationDelivery toDomain(ConvocationDeliveryEntity entity) {
        return ConvocationDelivery.reconstruct(ConvocationDeliveryId.of(entity.getId()),
                ChannelCode.of(entity.getChannelCode()), entity.getStatus(), entity.getSentAt(), entity.getReference(),
                entity.isReminder(),
                entity.getRecordedByUserId() != null ? EntityId.of(entity.getRecordedByUserId()) : null,
                entity.getCreatedDate());
    }

    default Convocation toDomain(ConvocationEntity entity, List<ConvocationDelivery> deliveries,
                                  List<ConvocationReply> replies) {
        return Convocation.reconstruct(ConvocationId.of(entity.getId()),
                GeneralMeetingId.of(entity.getGeneralMeetingId()), EntityId.of(entity.getUnitId()),
                VotingWeight.of(entity.getVotingWeight()), deliveries, replies, entity.getConfirmationToken(),
                ShortCode.of(entity.getConfirmationCode()), entity.getAttendanceReply(),
                entity.getRepliedAt(), entity.getReplySource(),
                entity.getRepliedByPartyId() != null ? EntityId.of(entity.getRepliedByPartyId()) : null,
                entity.getReplyNote(),
                entity.getReplyMedium() != null ? ReplyMediumCode.of(entity.getReplyMedium()) : null,
                entity.getReplyAttendanceMode(), entity.isReplyByProxy(),
                entity.isCheckedIn(), entity.getAttendanceMode(),
                entity.getCheckedInPartyId() != null ? EntityId.of(entity.getCheckedInPartyId()) : null,
                entity.getCheckedInAt(), entity.getCreatedDate());
    }

    default ConvocationReplyEntity toEntity(ConvocationReply reply, ConvocationId convocationId) {
        ConvocationReplyEntity entity = new ConvocationReplyEntity();
        entity.setId(reply.getId().asUuid());
        entity.setConvocationId(convocationId.asUuid());
        entity.setAttendanceReply(reply.getReply());
        entity.setAttendanceMode(reply.getAttendanceMode());
        entity.setByProxy(reply.isByProxy());
        entity.setReplySource(reply.getSource());
        entity.setReplyMedium(reply.getMedium() != null ? reply.getMedium().value() : null);
        entity.setRepliedByPartyId(reply.getRepliedByPartyId() != null ? reply.getRepliedByPartyId().value() : null);
        entity.setNote(reply.getNote());
        entity.setReceivedAt(reply.getReceivedAt());
        entity.setRecordedByUserId(reply.getRecordedByUserId() != null ? reply.getRecordedByUserId().value() : null);
        return entity;
    }

    default ConvocationReply toDomain(ConvocationReplyEntity entity) {
        return ConvocationReply.reconstruct(ConvocationReplyId.of(entity.getId()), entity.getAttendanceReply(),
                entity.getAttendanceMode(), entity.isByProxy(), entity.getReplySource(),
                entity.getReplyMedium() != null ? ReplyMediumCode.of(entity.getReplyMedium()) : null,
                entity.getRepliedByPartyId() != null ? EntityId.of(entity.getRepliedByPartyId()) : null,
                entity.getNote(), entity.getReceivedAt(),
                entity.getRecordedByUserId() != null ? EntityId.of(entity.getRecordedByUserId()) : null,
                entity.getCreatedDate());
    }
}
