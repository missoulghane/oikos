package com.architek.oikos.meeting.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;

/** reference carries a registered letter's tracking number, when the channel produces one. */
public record RecordConvocationDeliveryRequest(@NotBlank String channel, @NotNull DeliveryStatus deliveryStatus,
                                                @Size(max = 100) String reference) {

    public ChannelCode channelCode() {
        return ChannelCode.of(channel);
    }
}
