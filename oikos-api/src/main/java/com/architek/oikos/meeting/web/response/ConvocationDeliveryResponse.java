package com.architek.oikos.meeting.web.response;

import java.time.Instant;

import com.architek.oikos.meeting.application.dto.ConvocationDeliveryView;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;

public record ConvocationDeliveryResponse(String id, String channelCode, String channelLabel, DeliveryStatus status,
                                           Instant sentAt, String reference) {

    public static ConvocationDeliveryResponse from(ConvocationDeliveryView view) {
        return new ConvocationDeliveryResponse(view.id(), view.channelCode(), view.channelLabel(), view.status(),
                view.sentAt(), view.reference());
    }
}
