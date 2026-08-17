package com.architek.oikos.meeting.web.response;

import com.architek.oikos.meeting.domain.model.ReplyMedium;

/**
 * A catalog row as the clients read it. No `automated` counterpart to the
 * channels': nothing is ever emitted through a medium, so there is no
 * behaviour for a client to key off - only a label to show.
 */
public record ReplyMediumResponse(String code, String label, int position) {

    public static ReplyMediumResponse from(ReplyMedium medium) {
        return new ReplyMediumResponse(medium.getCode().value(), medium.getLabel(), medium.getPosition());
    }
}
