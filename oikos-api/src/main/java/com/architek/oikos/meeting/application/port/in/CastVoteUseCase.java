package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.CastVoteCommand;
import com.architek.oikos.meeting.application.dto.VoteView;

public interface CastVoteUseCase {

    VoteView cast(CastVoteCommand command);
}
