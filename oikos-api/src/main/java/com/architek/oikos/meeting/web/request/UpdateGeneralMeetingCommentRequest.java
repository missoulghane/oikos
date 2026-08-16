package com.architek.oikos.meeting.web.request;

import jakarta.validation.constraints.Size;

/**
 * The editor's HTML output, carried as-is. Not validated beyond a length cap:
 * the product's convention for rich text is that the wire format is whatever
 * the editor produced, and the security boundary is the sanitize pass at
 * display time - the same arrangement MessageBody documents.
 */
public record UpdateGeneralMeetingCommentRequest(@Size(max = 20000) String comment) {
}
