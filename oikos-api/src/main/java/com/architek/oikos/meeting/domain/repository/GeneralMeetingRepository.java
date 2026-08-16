package com.architek.oikos.meeting.domain.repository;

import java.util.Optional;

import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface GeneralMeetingRepository {

    /**
     * The meeting a copropriétaire named on the public form. Also what the code
     * generator checks against, since the reference is unique across the whole
     * product rather than per property.
     */
    Optional<GeneralMeeting> findByPublicReference(ShortCode publicReference);


    GeneralMeeting save(GeneralMeeting generalMeeting);

    Optional<GeneralMeeting> findById(GeneralMeetingId id);

    void deleteById(GeneralMeetingId id);

    /**
     * Meetings of one property, most recent first. status and meetingType are
     * optional filters - null means "no filter on this criterion" rather than
     * "matches null", there being no such thing as a meeting without either.
     */
    Page<GeneralMeeting> findByProperty(EntityId propertyId, MeetingStatus status, MeetingType meetingType,
                                         PageRequest pageRequest);
}
