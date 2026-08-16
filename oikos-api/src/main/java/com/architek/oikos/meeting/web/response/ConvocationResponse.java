package com.architek.oikos.meeting.web.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.architek.oikos.meeting.application.dto.ConvocationRecipient;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ConvocationStatus;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;

/**
 * deliveries is the history, deliveryStatus and sentAt the summary of it. Both
 * ship: the tracking table has one column for the state of the sending, the
 * detail page shows every attempt that produced it.
 *
 * <p>meetingPublicReference and confirmationCode are null on every list
 * response and populated only on GET /convocations/{id} - see
 * ConvocationView.withoutCodes for why a hundred codes must not travel
 * together.
 */
public record ConvocationResponse(String id, String generalMeetingId, String unitId, String unitNumber,
                                   String buildingName, List<ConvocationRecipient> recipients, BigDecimal votingWeight,
                                   List<ConvocationDeliveryResponse> deliveries, Instant sentAt,
                                   DeliveryStatus deliveryStatus, AttendanceReply attendanceReply, Instant repliedAt,
                                   ReplySource replySource, String replyNote, boolean checkedIn,
                                   AttendanceMode attendanceMode, Instant checkedInAt, ConvocationStatus status,
                                   String meetingPublicReference, String confirmationCode) {

    public static ConvocationResponse from(ConvocationView view) {
        return new ConvocationResponse(view.id().toString(), view.generalMeetingId().toString(),
                view.unitId().toString(), view.unitNumber(), view.buildingName(), view.recipients(),
                view.votingWeight(), view.deliveries().stream().map(ConvocationDeliveryResponse::from).toList(),
                view.sentAt(), view.deliveryStatus(), view.attendanceReply(), view.repliedAt(), view.replySource(),
                view.replyNote(), view.checkedIn(), view.attendanceMode(), view.checkedInAt(), view.status(),
                view.meetingPublicReference(), view.confirmationCode());
    }
}
