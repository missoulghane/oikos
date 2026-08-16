package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.ReplyToConvocationCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;

public interface ReplyToConvocationUseCase {

    ConvocationView reply(ReplyToConvocationCommand command);
}
