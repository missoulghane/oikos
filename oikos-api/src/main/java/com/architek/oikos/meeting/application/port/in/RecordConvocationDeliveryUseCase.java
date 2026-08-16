package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.command.RecordConvocationDeliveryCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;

public interface RecordConvocationDeliveryUseCase {

    ConvocationView record(RecordConvocationDeliveryCommand command);
}
