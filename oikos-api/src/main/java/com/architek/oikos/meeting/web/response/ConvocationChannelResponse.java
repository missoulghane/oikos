package com.architek.oikos.meeting.web.response;

import com.architek.oikos.meeting.domain.model.ConvocationChannel;

/**
 * A catalog row as the clients read it. automated is what the screens key off:
 * an automated channel offers a "send" button, a manual one offers "record the
 * delivery" - and no client should have to keep its own list of which is which.
 */
public record ConvocationChannelResponse(String code, String label, boolean automated, int position) {

    public static ConvocationChannelResponse from(ConvocationChannel channel) {
        return new ConvocationChannelResponse(channel.getCode().value(), channel.getLabel(), channel.isAutomated(),
                channel.getPosition());
    }
}
