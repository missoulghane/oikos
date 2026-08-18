package com.architek.oikos.meeting.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;

/**
 * Whoever holds the link says whether the lot will be there - and proves, with
 * the six-character code printed beside the QR code on their convocation, that
 * they are answering for their own lot.
 *
 * <p>Separate from {@link ConfirmConvocationRequest} rather than a shared
 * record with an optional field: on the paper path the code is already in the
 * URL, and a body field that one of the two endpoints silently ignores is how a
 * required check quietly becomes optional.
 */
public record ConfirmConvocationByTokenRequest(@NotNull AttendanceReply attendanceReply,
                                                @NotBlank String confirmationCode) {
}
