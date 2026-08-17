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
 *
 * <p>{@link #isReminder()} says the attempt was a chase rather than the
 * convocation itself. A reminder <em>is</em> a send - same table, same
 * statuses, and it is recorded exactly like any other, which is what the
 * previous shape got wrong by sending reminders that left no trace at all.
 * The flag changes nothing that is computed: the convocation's derived status
 * ignores it, and {@link Convocation#getSentAt()} keeps naming the first
 * successful attempt, so a reminder can never push the notice period back.
 * It exists so the tracking table can say "(Relance)" instead of showing a
 * second send nobody ordered.
 */
public final class ConvocationDelivery {

    private final ConvocationDeliveryId id;
    private final ChannelCode channelCode;
    private final DeliveryStatus status;
    private final Instant sentAt;
    private final String reference;
    private final boolean reminder;
    private final EntityId recordedByUserId;
    private final Instant createdDate;

    private ConvocationDelivery(ConvocationDeliveryId id, ChannelCode channelCode, DeliveryStatus status,
                                 Instant sentAt, String reference, boolean reminder, EntityId recordedByUserId,
                                 Instant createdDate) {
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
        if (reminder && reference != null) {
            // A tracking number belongs to a registered letter, and the postal channels are
            // recorded by hand rather than chased in a run. A reminder carrying one would mean
            // the two paths had been confused somewhere upstream.
            throw new IllegalArgumentException("a reminder carries no tracking reference");
        }
        this.sentAt = sentAt;
        this.reference = reference;
        this.reminder = reminder;
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
                Objects.requireNonNull(at, "at must not be null"), reference, false, recordedByUserId, at);
    }

    /**
     * It did not. No date: a failed attempt never left, and stamping one would
     * read as "sent, then failed".
     */
    public static ConvocationDelivery failed(ConvocationDeliveryId id, ChannelCode channelCode, Instant at,
                                              EntityId recordedByUserId) {
        return new ConvocationDelivery(id, channelCode, DeliveryStatus.FAILED, null, null, false, recordedByUserId, at);
    }

    /** A chase that went out, on a lot already reached and still silent. */
    public static ConvocationDelivery reminderSent(ConvocationDeliveryId id, ChannelCode channelCode, Instant at,
                                                    EntityId recordedByUserId) {
        return new ConvocationDelivery(id, channelCode, DeliveryStatus.SENT,
                Objects.requireNonNull(at, "at must not be null"), null, true, recordedByUserId, at);
    }

    /**
     * A chase that could not go out. Worth a row like any other failure: it is
     * what tells the syndic that chasing this lot by this channel is pointless.
     */
    public static ConvocationDelivery reminderFailed(ConvocationDeliveryId id, ChannelCode channelCode, Instant at,
                                                      EntityId recordedByUserId) {
        return new ConvocationDelivery(id, channelCode, DeliveryStatus.FAILED, null, null, true, recordedByUserId, at);
    }

    public static ConvocationDelivery reconstruct(ConvocationDeliveryId id, ChannelCode channelCode,
                                                   DeliveryStatus status, Instant sentAt, String reference,
                                                   boolean reminder, EntityId recordedByUserId, Instant createdDate) {
        return new ConvocationDelivery(id, channelCode, status, sentAt, reference, reminder, recordedByUserId,
                createdDate);
    }

    public boolean isSent() {
        return status == DeliveryStatus.SENT;
    }

    /** A chase rather than the convocation itself. Display only - nothing computes on it. */
    public boolean isReminder() {
        return reminder;
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
