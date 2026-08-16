package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.SendConvocationCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;

public interface SendConvocationUseCase {

    ConvocationView send(SendConvocationCommand command);
}
