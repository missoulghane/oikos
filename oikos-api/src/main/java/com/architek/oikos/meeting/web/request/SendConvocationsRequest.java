package com.architek.oikos.meeting.web.request;

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;

/**
 * Only a channel the application can actually perform is accepted: the manual
 * ones are recorded lot by lot through PUT /convocations/{id}/delivery-status
 * once a person has actually posted something.
 */
public record SendConvocationsRequest(String channel) {

    public ChannelCode channelOrDefault() {
        return channel == null || channel.isBlank() ? ChannelCode.EMAIL : ChannelCode.of(channel);
    }
}
