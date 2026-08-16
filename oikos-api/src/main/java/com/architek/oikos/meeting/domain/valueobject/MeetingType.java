package com.architek.oikos.meeting.domain.valueobject;

/**
 * Nature of the meeting. Beyond labelling, it selects which quorum threshold
 * applies (see MeetingQuorumSetting, one row per property and per type).
 */
public enum MeetingType {
    ORDINARY,
    EXTRAORDINARY
}
