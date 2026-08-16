package com.architek.oikos.meeting.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The minutes as HTML, written in a rich-text editor. Generous limit: a
 * contested assembly's record runs long, and truncating one silently would
 * lose exactly the passages worth keeping.
 */
public record UpdateMeetingMinutesRequest(@NotBlank @Size(max = 200000) String content) {
}
