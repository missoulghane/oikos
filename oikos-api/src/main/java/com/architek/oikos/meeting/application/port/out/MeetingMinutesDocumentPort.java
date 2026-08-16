package com.architek.oikos.meeting.application.port.out;

import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Files the final PDF through the document module, owned by the meeting
 * (DocumentOwnerType.MEETING_MINUTES). Replaces rather than accumulates, for
 * the same reason as the convocation: the document module refuses a
 * byte-identical upload for the same owner.
 */
public interface MeetingMinutesDocumentPort {

    void replaceMinutesDocument(GeneralMeetingId generalMeetingId, String fileName, byte[] content,
                                 EntityId uploadedByUserId);
}
