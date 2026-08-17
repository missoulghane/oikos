package com.architek.oikos.meeting.domain.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;

import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ConvocationReplyId;
import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * One answer given for a lot, as an event.
 *
 * <p>A convocation carries a list of these because people change their mind,
 * and because a contested AG turns on being able to show what was said and
 * when. The convocation also keeps the latest one denormalised on its own
 * fields - see {@link Convocation#reply} for why that duplication is deliberate
 * and how it is kept honest.
 *
 * <p><b>Two dates, not one.</b> {@code receivedAt} is when the answer was
 * given, declared by whoever records it and freely backdated - a letter that
 * arrived on Tuesday is entered on Thursday. {@code createdDate} is when it was
 * typed, and the audit layer owns it. Collapsing them would force a choice
 * between two wrong behaviours: order by the typing alone and an old letter
 * keyed in late overrides a more recent phone call; order by the declared date
 * alone and two answers claiming the same instant - one of them a correction of
 * the other - cannot be told apart. {@link #LATEST_FIRST} uses both, in that
 * order.
 *
 * <p><b>Withdrawals are events too.</b> An entry may carry
 * {@link AttendanceReply#NO_REPLY}: "the syndic took the answer back on the
 * 15th" is a fact, and losing it is exactly what this history exists to
 * prevent. Note the asymmetry with the convocation's own fields, which do drop
 * the provenance of a withdrawal - there, no answer stands, so no source can
 * describe it. Here the source describes the act of withdrawing.
 *
 * <p><b>What an answer announces, and what it does not.</b> attendanceMode says
 * how the lot means to attend - in the room or remotely - and byProxy that
 * somebody else will stand in. Both describe an <em>intention</em> declared
 * before the session and neither is worth a voice: presence is the check-in,
 * and having answered "attending" has never granted the right to vote
 * (ADR 0002 §5). byProxy in particular is NOT the mandate: the procuration
 * itself - who holds it, and whose voice it carries - remains out of scope
 * (ADR 0002 §7). What is recorded here is that the syndic was told a
 * representative would come, which is what makes a room prepared for rather
 * than a vote counted.
 *
 * <p>Immutable, and never modified after the fact.
 */
public final class ConvocationReply {

    /**
     * Most recent first: by when the answer was given, then by when it was
     * recorded. The head of a list sorted this way is the answer that stands.
     */
    public static final Comparator<ConvocationReply> LATEST_FIRST = Comparator
            .comparing(ConvocationReply::getReceivedAt)
            .thenComparing(ConvocationReply::getCreatedDate, Comparator.nullsFirst(Comparator.naturalOrder()))
            .reversed();

    private final ConvocationReplyId id;
    private final AttendanceReply reply;
    private final AttendanceMode attendanceMode;
    private final boolean byProxy;
    private final ReplySource source;
    private final ReplyMediumCode medium;
    private final EntityId repliedByPartyId;
    private final String note;
    private final Instant receivedAt;
    private final EntityId recordedByUserId;
    private final Instant createdDate;

    private ConvocationReply(ConvocationReplyId id, AttendanceReply reply, AttendanceMode attendanceMode,
                              boolean byProxy, ReplySource source, ReplyMediumCode medium,
                              EntityId repliedByPartyId, String note, Instant receivedAt, EntityId recordedByUserId,
                              Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.reply = Objects.requireNonNull(reply, "reply must not be null");
        // Only an answer that announces a presence can say how, or that someone will stand in.
        // "Absent, sur place" and "absent par procuration" are not states of the world.
        if (reply != AttendanceReply.ATTENDING && (attendanceMode != null || byProxy)) {
            throw new IllegalArgumentException(
                    "only an attending reply carries an attendance mode or a proxy announcement");
        }
        this.attendanceMode = attendanceMode;
        this.byProxy = byProxy;
        // Unlike the convocation's own columns, a NO_REPLY entry keeps its source: it records
        // an act of withdrawal, which somebody performed and which is worth attributing.
        this.source = Objects.requireNonNull(source, "source must not be null");
        if (medium != null && source != ReplySource.OTHER) {
            // A medium answers "by what means did it reach the office", which only has meaning
            // once the answer did not come through the app or the link. Allowing it elsewhere
            // would let a row claim an owner confirmed from their space "by telephone".
            throw new IllegalArgumentException("a reply medium only describes a reply whose source is OTHER");
        }
        this.medium = medium;
        this.repliedByPartyId = repliedByPartyId;
        this.note = note;
        this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt must not be null");
        this.recordedByUserId = recordedByUserId;
        this.createdDate = createdDate;
    }

    /**
     * An answer as it is recorded. medium is optional even when the source is
     * OTHER: forcing a choice on a syndic who was simply told "he is not
     * coming" would buy a filled column and no truth.
     */
    public static ConvocationReply record(ConvocationReplyId id, AttendanceReply reply, AttendanceMode attendanceMode,
                                           boolean byProxy, ReplySource source, ReplyMediumCode medium,
                                           EntityId repliedByPartyId, String note, Instant receivedAt,
                                           EntityId recordedByUserId) {
        return new ConvocationReply(id, reply, attendanceMode, byProxy, source, medium, repliedByPartyId, note,
                receivedAt, recordedByUserId, receivedAt);
    }

    public static ConvocationReply reconstruct(ConvocationReplyId id, AttendanceReply reply,
                                                AttendanceMode attendanceMode, boolean byProxy, ReplySource source,
                                                ReplyMediumCode medium, EntityId repliedByPartyId, String note,
                                                Instant receivedAt, EntityId recordedByUserId, Instant createdDate) {
        return new ConvocationReply(id, reply, attendanceMode, byProxy, source, medium, repliedByPartyId, note,
                receivedAt, recordedByUserId, createdDate);
    }

    /** A withdrawal rather than an answer - nothing stands after it. */
    public boolean isWithdrawal() {
        return reply == AttendanceReply.NO_REPLY;
    }

    public ConvocationReplyId getId() {
        return id;
    }

    public AttendanceReply getReply() {
        return reply;
    }

    /** How the lot announced it would attend. Null unless the answer is ATTENDING. */
    public AttendanceMode getAttendanceMode() {
        return attendanceMode;
    }

    /**
     * Somebody will stand in for the lot. An announcement, not a mandate: the
     * procuration itself is still out of scope (ADR 0002 §7), and nothing here
     * carries a voice.
     */
    public boolean isByProxy() {
        return byProxy;
    }

    public ReplySource getSource() {
        return source;
    }

    public ReplyMediumCode getMedium() {
        return medium;
    }

    public EntityId getRepliedByPartyId() {
        return repliedByPartyId;
    }

    public String getNote() {
        return note;
    }

    /** When the answer was given, as declared - may predate the recording. */
    public Instant getReceivedAt() {
        return receivedAt;
    }

    public EntityId getRecordedByUserId() {
        return recordedByUserId;
    }

    /** When it was typed. Owned by the audit layer; null until persisted. */
    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof ConvocationReply other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
