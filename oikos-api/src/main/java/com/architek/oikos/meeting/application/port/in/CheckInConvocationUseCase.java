package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.CheckInConvocationCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;

public interface CheckInConvocationUseCase {

    ConvocationView checkIn(CheckInConvocationCommand command);

    ConvocationView undoCheckIn(CheckInConvocationCommand command);
}
