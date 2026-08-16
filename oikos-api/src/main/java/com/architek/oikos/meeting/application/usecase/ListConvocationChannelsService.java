package com.architek.oikos.meeting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.port.in.ListConvocationChannelsUseCase;
import com.architek.oikos.meeting.domain.model.ConvocationChannel;
import com.architek.oikos.meeting.domain.repository.ConvocationChannelRepository;

/**
 * Active rows only, in display order. A deactivated channel still resolves by
 * code - past deliveries reference it - but it is not offered for a new send.
 */
@Component
public class ListConvocationChannelsService implements ListConvocationChannelsUseCase {

    private final ConvocationChannelRepository channelRepository;

    public ListConvocationChannelsService(ConvocationChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConvocationChannel> listChannels() {
        return channelRepository.findAllActive();
    }
}
