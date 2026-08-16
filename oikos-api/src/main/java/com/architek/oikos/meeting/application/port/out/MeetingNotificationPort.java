package com.architek.oikos.meeting.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Posts the in-app notification that a meeting has been convened. Kept behind
 * a port so the notification module's command/type vocabulary never reaches
 * this module's use cases (rule 4/6).
 */
public interface MeetingNotificationPort {

    void notifyGeneralMeetingCalled(EntityId recipientUserId, EntityId propertyId, String meetingTitle,
                                     String linkPath);

    /**
     * Both notifications share NotificationType.GENERAL_MEETING_CALLED: the
     * catalog has one constant for the AG lifecycle and adding a second is a
     * schema change to the enum, not something to slip in for a wording
     * difference. They differ by title and body, which is what the bell shows.
     */
    void notifyMinutesPublished(EntityId recipientUserId, EntityId propertyId, String meetingTitle, String linkPath);
}
