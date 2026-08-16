package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.RemindPendingConvocationsCommand;

public interface RemindPendingConvocationsUseCase {

    /** Number of convocations a reminder actually went out to. */
    int remind(RemindPendingConvocationsCommand command);
}
