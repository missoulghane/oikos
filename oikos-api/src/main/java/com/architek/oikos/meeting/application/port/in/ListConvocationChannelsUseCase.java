package com.architek.oikos.meeting.application.port.in;

import java.util.List;

import com.architek.oikos.meeting.domain.model.ConvocationChannel;

/** The channels on offer, for the screens that have to let someone pick one. */
public interface ListConvocationChannelsUseCase {

    List<ConvocationChannel> listChannels();
}
