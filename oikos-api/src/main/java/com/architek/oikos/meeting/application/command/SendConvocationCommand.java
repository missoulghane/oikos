package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record SendConvocationCommand(ConvocationId convocationId, ChannelCode channel, EntityId requestedByUserId) {
}
