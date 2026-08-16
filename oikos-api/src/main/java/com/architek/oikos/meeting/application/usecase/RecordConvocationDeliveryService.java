package com.architek.oikos.meeting.application.usecase;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.RecordConvocationDeliveryCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.RecordConvocationDeliveryUseCase;
import com.architek.oikos.meeting.domain.model.ConvocationDelivery;
import com.architek.oikos.meeting.domain.valueobject.ConvocationDeliveryId;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;

/**
 * "I posted the letter" - the manual counterpart of SendConvocationUseCase,
 * and a deliberately separate verb rather than a flag on it. A recorded
 * delivery asserts something a person did; a send performs it. Merging the two
 * would make the tracking table unable to tell them apart, which is precisely
 * what it exists to do.
 *
 * <p>Not restricted to the postal channels: a syndic who handed the
 * convocation over in person, or sent it from their own mailbox, records it
 * here too. The channel must exist in the catalog, but it need not be manual -
 * recording that an email was sent by hand is a legitimate thing to assert.
 *
 * <p>Each call appends an attempt; it never overwrites the previous one. A lot
 * emailed on Monday and posted on Friday carries both, which is the whole point
 * of the change.
 */
@Component
public class RecordConvocationDeliveryService implements RecordConvocationDeliveryUseCase {

    private final ConvocationLookup lookup;
    private final ConvocationChannelLookup channelLookup;
    private final Clock clock;

    public RecordConvocationDeliveryService(ConvocationLookup lookup, ConvocationChannelLookup channelLookup,
                                             Clock clock) {
        this.lookup = lookup;
        this.channelLookup = channelLookup;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ConvocationView record(RecordConvocationDeliveryCommand command) {
        channelLookup.require(command.channel());
        Instant now = clock.instant();
        ConvocationDeliveryId deliveryId = ConvocationDeliveryId.newId();
        ConvocationDelivery delivery = command.deliveryStatus() == DeliveryStatus.SENT
                ? ConvocationDelivery.sent(deliveryId, command.channel(), now, command.reference(),
                        command.recordedByUserId())
                : ConvocationDelivery.failed(deliveryId, command.channel(), now, command.recordedByUserId());
        return lookup.save(lookup.require(command.convocationId()).recordDelivery(delivery));
    }
}
