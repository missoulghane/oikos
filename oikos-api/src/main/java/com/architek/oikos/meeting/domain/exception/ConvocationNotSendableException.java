package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.shared.exception.BusinessException;

/**
 * Asking the application to perform a send it cannot perform. Two cases, and
 * the message tells them apart because the fix differs:
 *
 * <ul>
 * <li>a manual channel (post, registered mail, handed over) - it is recorded
 * through the delivery-status endpoint instead, once a person has actually
 * posted something. Pressing a button must never be able to claim a letter
 * left the building.</li>
 * <li>a channel the catalog marks automated with no emitter behind it. Since
 * the channels became data, someone can add "SMS, automated" with an INSERT;
 * failing loudly here is what keeps that row from silently sending nothing.</li>
 * </ul>
 */
public class ConvocationNotSendableException extends BusinessException {

    public ConvocationNotSendableException(ChannelCode channel) {
        super("Channel " + channel + " cannot be sent by the application - record its delivery status manually "
                + "with PUT /convocations/{id}/delivery-status");
    }

    public static ConvocationNotSendableException noEmitter(ChannelCode channel) {
        return new ConvocationNotSendableException(
                "Channel " + channel + " is catalogued as automated but no emitter implements it - either implement "
                        + "one or set convocation_channel.automated to false for that row");
    }

    private ConvocationNotSendableException(String message) {
        super(message);
    }
}
