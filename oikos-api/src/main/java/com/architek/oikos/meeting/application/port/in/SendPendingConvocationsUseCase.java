package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.SendPendingConvocationsCommand;

public interface SendPendingConvocationsUseCase {

    /** How many convocations actually went out, and how many failed. */
    SendPendingConvocationsResult send(SendPendingConvocationsCommand command);

    record SendPendingConvocationsResult(int sentCount, int failedCount) {
    }
}
