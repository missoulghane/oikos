package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.UpdateGeneralMeetingCommentCommand;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;

public interface UpdateGeneralMeetingCommentUseCase {

    GeneralMeetingView updateComment(UpdateGeneralMeetingCommentCommand command);
}
