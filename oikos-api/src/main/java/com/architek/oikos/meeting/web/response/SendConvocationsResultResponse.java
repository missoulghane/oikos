package com.architek.oikos.meeting.web.response;

import com.architek.oikos.meeting.application.port.in.SendPendingConvocationsUseCase.SendPendingConvocationsResult;

/**
 * Failures are counted, not thrown: one unreachable lot must not fail the run,
 * and the syndic reads the detail in the tracking table (those rows are FAILED).
 */
public record SendConvocationsResultResponse(int sentCount, int failedCount) {

    public static SendConvocationsResultResponse from(SendPendingConvocationsResult result) {
        return new SendConvocationsResultResponse(result.sentCount(), result.failedCount());
    }
}
