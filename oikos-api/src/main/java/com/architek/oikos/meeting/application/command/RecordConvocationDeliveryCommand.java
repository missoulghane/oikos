package com.architek.oikos.meeting.application.command;

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Manual recording, for the channels the application cannot perform itself.
 * reference carries a registered letter's tracking number when there is one.
 */
public record RecordConvocationDeliveryCommand(ConvocationId convocationId, ChannelCode channel,
                                                DeliveryStatus deliveryStatus, String reference,
                                                EntityId recordedByUserId) {
}
