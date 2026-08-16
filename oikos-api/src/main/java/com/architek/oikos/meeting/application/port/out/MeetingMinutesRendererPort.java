package com.architek.oikos.meeting.application.port.out;

/**
 * Renders the already-composed minutes to PDF. Takes the frozen HTML content
 * and the meeting's identity, nothing else: what is printed must be what was
 * validated, never a fresh recomputation of the figures.
 */
public interface MeetingMinutesRendererPort {

    byte[] render(String propertyName, String meetingTitle, String htmlContent);
}
