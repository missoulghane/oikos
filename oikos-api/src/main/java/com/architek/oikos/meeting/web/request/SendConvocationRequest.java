package com.architek.oikos.meeting.web.request;

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;

/**
 * The channel is a catalog code, not an enum constant: the list is data now, so
 * validating it here against a fixed set would defeat the point. An unknown
 * code is a 404 from the catalog, a manual one a business error.
 */
public record SendConvocationRequest(String channel) {

    public ChannelCode channelOrDefault() {
        return channel == null || channel.isBlank() ? ChannelCode.EMAIL : ChannelCode.of(channel);
    }
}
