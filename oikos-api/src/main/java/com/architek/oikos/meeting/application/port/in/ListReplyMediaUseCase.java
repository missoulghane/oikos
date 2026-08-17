package com.architek.oikos.meeting.application.port.in;

import java.util.List;

import com.architek.oikos.meeting.domain.model.ReplyMedium;

/** The means of reception on offer, for the screen that records an answer taken at the office. */
public interface ListReplyMediaUseCase {

    List<ReplyMedium> listMedia();
}
