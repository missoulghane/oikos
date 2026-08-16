package com.architek.oikos.meeting.application.port.in;

import java.util.List;

import com.architek.oikos.meeting.application.command.GenerateConvocationsCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;

public interface GenerateConvocationsUseCase {

    List<ConvocationView> generate(GenerateConvocationsCommand command);
}
