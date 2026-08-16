package com.architek.oikos.meeting.application.dto;

import java.time.Instant;

import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;

/**
 * One attempt, as a screen shows it. channelLabel is resolved from the catalog
 * here rather than left to the client: the label is the catalog's to define,
 * and a front-end map of codes to labels would have to be redeployed every time
 * a channel is added - which is precisely what making the channels data was
 * meant to avoid.
 */
public record ConvocationDeliveryView(String id, String channelCode, String channelLabel, DeliveryStatus status,
                                       Instant sentAt, String reference) {
}
