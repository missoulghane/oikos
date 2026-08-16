package com.architek.oikos.meeting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationStatus;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * One row of the tracking table. unitNumber, buildingName and recipients come
 * from the caller (resolved through PropertyUnitDirectoryPort): the aggregate
 * knows only the lot's id, and a screen listing "Lot 3fa85f64-..." would be
 * useless. status, deliveryStatus and sentAt are derived by the aggregate.
 *
 * <p>deliveries carries every attempt; deliveryStatus and sentAt summarise them
 * for the columns that only have room for one value. Both are kept: the table
 * needs the summary, the detail page needs the history.
 */
public record ConvocationView(ConvocationId id, GeneralMeetingId generalMeetingId, EntityId unitId, String unitNumber,
                               String buildingName, List<ConvocationRecipient> recipients, BigDecimal votingWeight,
                               List<ConvocationDeliveryView> deliveries, Instant sentAt, DeliveryStatus deliveryStatus,
                               AttendanceReply attendanceReply, Instant repliedAt, ReplySource replySource,
                               String replyNote, boolean checkedIn, AttendanceMode attendanceMode, Instant checkedInAt,
                               ConvocationStatus status, String meetingPublicReference, String confirmationCode) {

    /**
     * The two short codes are the only thing on this record that is a secret,
     * and they are carried on ONE screen: a lot's detail page, where a syndic
     * reads them out to a copropriétaire who lost their letter. Every list path
     * calls {@link #withoutCodes()} - a hundred lots' codes in one payload is
     * the whole copropriété's answers handed to whoever can open that page.
     */
    public ConvocationView withoutCodes() {
        return new ConvocationView(id, generalMeetingId, unitId, unitNumber, buildingName, recipients, votingWeight,
                deliveries, sentAt, deliveryStatus, attendanceReply, repliedAt, replySource, replyNote, checkedIn,
                attendanceMode, checkedInAt, status, null, null);
    }

    public static ConvocationView from(Convocation convocation, String unitNumber, String buildingName,
                                        List<ConvocationRecipient> recipients,
                                        List<ConvocationDeliveryView> deliveries, String meetingPublicReference) {
        return new ConvocationView(convocation.getId(), convocation.getGeneralMeetingId(), convocation.getUnitId(),
                unitNumber, buildingName, recipients, convocation.getVotingWeight().value(), deliveries,
                convocation.getSentAt(), convocation.getDeliveryStatus(), convocation.getAttendanceReply(),
                convocation.getRepliedAt(), convocation.getReplySource(), convocation.getReplyNote(),
                convocation.isCheckedIn(), convocation.getAttendanceMode(), convocation.getCheckedInAt(),
                convocation.status(), meetingPublicReference, convocation.getConfirmationCode().value());
    }
}
