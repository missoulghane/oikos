package com.architek.oikos.meeting.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationDeliveryId;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * One attempt at getting a convocation to its recipients, on one channel.
 *
 * <p>A convocation carries a list of these rather than a single channel and a
 * single date. The same convocation legitimately goes out several ways - an
 * email first, a registered letter afterwards for the lot that never answered -
 * and the previous shape overwrote the earlier one every time, losing the very
 * trace the syndic needs.
 *
 * <p>Failures are kept, not discarded: "we tried by email and there was no
 * address" is what tells the syndic to post a letter instead, and it is
 * indistinguishable from "nobody has tried yet" once it is thrown away.
 *
 * <p>Immutable, and never modified after the fact: a delivery is an event. A
 * second attempt on the same channel is a second row.
 */
public final class ConvocationDelivery {

    private final ConvocationDeliveryId id;
    private final ChannelCode channelCode;
    private final DeliveryStatus status;
    private final Instant sentAt;
    private final String reference;
    private final EntityId recordedByUserId;
    private final Instant createdDate;

    private ConvocationDelivery(ConvocationDeliveryId id, ChannelCode channelCode, DeliveryStatus status,
                                 Instant sentAt, String reference, EntityId recordedByUserId, Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.channelCode = Objects.requireNonNull(channelCode, "channelCode must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        if (status == DeliveryStatus.TO_SEND) {
            throw new IllegalArgumentException(
                    "TO_SEND is the absence of a delivery, not one of its outcomes - do not record it as a row");
        }
        if ((status == DeliveryStatus.SENT) != (sentAt != null)) {
            throw new IllegalArgumentException("a successful delivery carries its date, a failed one has none");
        }
        this.sentAt = sentAt;
        this.reference = reference;
        this.recordedByUserId = recordedByUserId;
        this.createdDate = createdDate;
    }

    /**
     * It went out. reference carries a tracking number when the channel
     * produces one (registered mail), null otherwise.
     */
    public static ConvocationDelivery sent(ConvocationDeliveryId id, ChannelCode channelCode, Instant at,
                                            String reference, EntityId recordedByUserId) {
        return new ConvocationDelivery(id, channelCode, DeliveryStatus.SENT,
                Objects.requireNonNull(at, "at must not be null"), reference, recordedByUserId, at);
    }

    /**
     * It did not. No date: a failed attempt never left, and stamping one would
     * read as "sent, then failed".
     */
    public static ConvocationDelivery failed(ConvocationDeliveryId id, ChannelCode channelCode, Instant at,
                                              EntityId recordedByUserId) {
        return new ConvocationDelivery(id, channelCode, DeliveryStatus.FAILED, null, null, recordedByUserId, at);
    }

    public static ConvocationDelivery reconstruct(ConvocationDeliveryId id, ChannelCode channelCode,
                                                   DeliveryStatus status, Instant sentAt, String reference,
                                                   EntityId recordedByUserId, Instant createdDate) {
        return new ConvocationDelivery(id, channelCode, status, sentAt, reference, recordedByUserId, createdDate);
    }

    public boolean isSent() {
        return status == DeliveryStatus.SENT;
    }

    public ConvocationDeliveryId getId() {
        return id;
    }

    public ChannelCode getChannelCode() {
        return channelCode;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public String getReference() {
        return reference;
    }

    public EntityId getRecordedByUserId() {
        return recordedByUserId;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof ConvocationDelivery other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
