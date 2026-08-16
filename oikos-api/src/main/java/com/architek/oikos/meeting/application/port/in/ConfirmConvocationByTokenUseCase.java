package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.ConfirmConvocationByTokenCommand;
import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;

/** Anonymous confirmation of presence, for a copropriétaire with no account. */
public interface ConfirmConvocationByTokenUseCase {

    ConvocationConfirmationView confirm(ConfirmConvocationByTokenCommand command);
}
