package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.ConfirmConvocationByCodeCommand;
import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;

/** Anonymous confirmation, for the copropriétaire who typed their code rather than following a link. */
public interface ConfirmConvocationByCodeUseCase {

    ConvocationConfirmationView confirm(ConfirmConvocationByCodeCommand command);
}
