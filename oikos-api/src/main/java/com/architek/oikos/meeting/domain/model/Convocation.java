package com.architek.oikos.meeting.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationStatus;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;
import com.architek.oikos.meeting.domain.valueobject.VotingWeight;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * One lot's whole journey for one meeting: the convocation sent to it, the
 * answer it gave, and its presence in the room. The SFD fuses those three
 * into a single object and so does this class.
 *
 * <p>The subject is the LOT, not the owner (ADR 0002 §2). A lot held in
 * indivision receives one convocation and carries one voice; an owner of three
 * lots receives three. The owners themselves are never stored here - they are
 * resolved from the property's current ownership records at the moment of
 * sending, so a sale between the convocation and the session leaves nothing to
 * migrate. checkedInPartyId and repliedByPartyId are the exceptions, and both
 * record a fact ("this person turned up", "this person answered"), not a right.
 *
 * <p>votingWeight is a snapshot: 1 under PER_UNIT, the lot's tantièmes under
 * SHARES. Without it, a sale, a correction of tantièmes or a change of mode
 * would make a past meeting's count irreproducible.
 *
 * <p>Sending is a LIST of {@link ConvocationDelivery}, not a single channel and
 * date: the same convocation goes out by email and then by registered mail for
 * the lot that stayed silent, and every attempt has to remain readable.
 * {@link #getDeliveryStatus()} and {@link #getSentAt()} are derived from that
 * list, and {@link #status()} from the three groups of fields - none of them
 * are stored, the SFD's statut_global included.
 *
 * <p>Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based.
 */
public final class Convocation {

    private final ConvocationId id;
    private final GeneralMeetingId generalMeetingId;
    private final EntityId unitId;
    private final VotingWeight votingWeight;
    private final List<ConvocationDelivery> deliveries;
    private final String confirmationToken;
    private final AttendanceReply attendanceReply;
    private final Instant repliedAt;
    private final ReplySource replySource;
    private final EntityId repliedByPartyId;
    private final String replyNote;
    private final boolean checkedIn;
    private final AttendanceMode attendanceMode;
    private final EntityId checkedInPartyId;
    private final Instant checkedInAt;
    private final Instant createdDate;

    private Convocation(ConvocationId id, GeneralMeetingId generalMeetingId, EntityId unitId, VotingWeight votingWeight,
                         List<ConvocationDelivery> deliveries, String confirmationToken,
                         AttendanceReply attendanceReply, Instant repliedAt, ReplySource replySource,
                         EntityId repliedByPartyId, String replyNote, boolean checkedIn, AttendanceMode attendanceMode,
                         EntityId checkedInPartyId, Instant checkedInAt, Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.generalMeetingId = Objects.requireNonNull(generalMeetingId, "generalMeetingId must not be null");
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.votingWeight = Objects.requireNonNull(votingWeight, "votingWeight must not be null");
        this.attendanceReply = Objects.requireNonNull(attendanceReply, "attendanceReply must not be null");
        if (checkedIn && (attendanceMode == null || checkedInAt == null)) {
            throw new IllegalArgumentException("a checked-in convocation must carry an attendance mode and a timestamp");
        }
        // Mirrors the CHECK constraint the table carries: an answer names how it was obtained, and
        // "has not answered" cannot have been obtained any way at all.
        if ((attendanceReply == AttendanceReply.NO_REPLY) != (replySource == null)) {
            throw new IllegalArgumentException("a recorded reply must carry its source, and no reply must carry none");
        }
        this.deliveries = List.copyOf(Objects.requireNonNull(deliveries, "deliveries must not be null"));
        this.confirmationToken = Objects.requireNonNull(confirmationToken, "confirmationToken must not be null");
        this.repliedAt = repliedAt;
        this.replySource = replySource;
        this.repliedByPartyId = repliedByPartyId;
        this.replyNote = replyNote;
        this.checkedIn = checkedIn;
        this.attendanceMode = attendanceMode;
        this.checkedInPartyId = checkedInPartyId;
        this.checkedInAt = checkedInAt;
        this.createdDate = createdDate;
    }

    /**
     * A freshly generated convocation: nothing sent, nobody has answered,
     * nobody has signed in.
     *
     * <p>The confirmation token is required from the start, not minted at send
     * time: the link it carries has to be printed on the letter itself,
     * including the one a syndic prints to post. A convocation without a token
     * would be a letter without a link.
     */
    public static Convocation generate(ConvocationId id, GeneralMeetingId generalMeetingId, EntityId unitId,
                                        VotingWeight votingWeight, String confirmationToken) {
        return new Convocation(id, generalMeetingId, unitId, votingWeight, List.of(), confirmationToken,
                AttendanceReply.NO_REPLY, null, null, null, null, false, null, null, null, null);
    }

    public static Convocation reconstruct(ConvocationId id, GeneralMeetingId generalMeetingId, EntityId unitId,
                                           VotingWeight votingWeight, List<ConvocationDelivery> deliveries,
                                           String confirmationToken, AttendanceReply attendanceReply,
                                           Instant repliedAt, ReplySource replySource, EntityId repliedByPartyId,
                                           String replyNote, boolean checkedIn, AttendanceMode attendanceMode,
                                           EntityId checkedInPartyId, Instant checkedInAt, Instant createdDate) {
        return new Convocation(id, generalMeetingId, unitId, votingWeight, deliveries, confirmationToken,
                attendanceReply, repliedAt, replySource, repliedByPartyId, replyNote, checkedIn, attendanceMode,
                checkedInPartyId, checkedInAt, createdDate);
    }

    /**
     * Adds an outcome of sending, whether the application performed it (EMAIL,
     * APP) or a person performed it and is recording the fact (post, registered
     * mail, handed over).
     *
     * <p>Appends, never replaces. Sending again by the same channel after a
     * failure is a second attempt and both belong in the record; sending by a
     * second channel must not erase the first.
     */
    public Convocation recordDelivery(ConvocationDelivery delivery) {
        Objects.requireNonNull(delivery, "delivery must not be null");
        List<ConvocationDelivery> updated = new ArrayList<>(deliveries);
        updated.add(delivery);
        return new Convocation(id, generalMeetingId, unitId, votingWeight, updated, confirmationToken,
                attendanceReply, repliedAt, replySource, repliedByPartyId, replyNote, checkedIn, attendanceMode,
                checkedInPartyId, checkedInAt, createdDate);
    }

    /**
     * The owner's answer, or the syndic entering it on their behalf (a lot
     * whose owners have no account still has to be tracked). Answering again
     * simply overwrites: people change their mind, and the last answer before
     * the session is the one that counts.
     *
     * <p>source says how the answer was obtained and is settled by the caller
     * from who is calling - never from what they claim. partyId names the
     * person who answered when it is known, and note carries the detail the
     * syndic would otherwise keep on a post-it ("called on Tuesday").
     */
    public Convocation reply(AttendanceReply newReply, ReplySource source, EntityId partyId, String note, Instant at) {
        Objects.requireNonNull(newReply, "reply must not be null");
        if (newReply == AttendanceReply.NO_REPLY) {
            // Taking the answer back takes its whole provenance with it: keeping a source for an
            // answer that no longer exists is the sort of leftover a contested AG makes expensive.
            return new Convocation(id, generalMeetingId, unitId, votingWeight, deliveries, confirmationToken,
                    newReply, null, null, null, null, checkedIn, attendanceMode, checkedInPartyId, checkedInAt,
                    createdDate);
        }
        Objects.requireNonNull(source, "reply source must not be null");
        return new Convocation(id, generalMeetingId, unitId, votingWeight, deliveries, confirmationToken, newReply,
                Objects.requireNonNull(at, "at must not be null"), source, partyId, note, checkedIn, attendanceMode,
                checkedInPartyId, checkedInAt, createdDate);
    }

    /**
     * Signing in at the opening of the session. This is what gives the lot the
     * right to vote, and what its weight counts towards for the quorum -
     * having answered "attending" beforehand grants neither.
     *
     * <p>partyId is optional: it names whoever turned up for the lot, which
     * matters in indivision, but a syndic ticking off a room does not always
     * know which co-owner it was.
     */
    public Convocation checkIn(AttendanceMode mode, EntityId partyId, Instant at) {
        Objects.requireNonNull(mode, "attendance mode must not be null");
        Objects.requireNonNull(at, "at must not be null");
        return new Convocation(id, generalMeetingId, unitId, votingWeight, deliveries, confirmationToken,
                attendanceReply, repliedAt, replySource, repliedByPartyId, replyNote, true, mode, partyId, at,
                createdDate);
    }

    /** Undoes a check-in entered by mistake - a room is ticked off by hand and hands slip. */
    public Convocation undoCheckIn() {
        return new Convocation(id, generalMeetingId, unitId, votingWeight, deliveries, confirmationToken,
                attendanceReply, repliedAt, replySource, repliedByPartyId, replyNote, false, null, null, null,
                createdDate);
    }

    /** The SFD's statut_global, derived from the three groups of fields above. */
    public ConvocationStatus status() {
        if (checkedIn) {
            return ConvocationStatus.CHECKED_IN;
        }
        if (attendanceReply != AttendanceReply.NO_REPLY) {
            return ConvocationStatus.CONFIRMED;
        }
        return getDeliveryStatus() == DeliveryStatus.SENT ? ConvocationStatus.SENT : ConvocationStatus.TO_SEND;
    }

    /**
     * Where the convocation stands on its way out, derived from its attempts:
     * none yet is TO_SEND, one that worked is SENT whatever failed alongside
     * it, and attempts that all failed are FAILED.
     *
     * <p>One success is enough: a lot reached by post is reached, however badly
     * the email went. The syndic needs to see the lots nobody could reach at
     * all, not the ones where one route out of two worked.
     */
    public DeliveryStatus getDeliveryStatus() {
        if (deliveries.isEmpty()) {
            return DeliveryStatus.TO_SEND;
        }
        return deliveries.stream().anyMatch(ConvocationDelivery::isSent) ? DeliveryStatus.SENT : DeliveryStatus.FAILED;
    }

    /**
     * The date the convocation went out: the FIRST successful attempt, not the
     * last. A reminder or a second channel must not push it back - that date is
     * what a contested AG turns on, since the notice period is counted from it.
     */
    public Instant getSentAt() {
        return deliveries.stream().filter(ConvocationDelivery::isSent).map(ConvocationDelivery::getSentAt)
                .min(Comparator.naturalOrder()).orElse(null);
    }

    /** Selects the population a reminder run targets: reached, but still silent. */
    public boolean awaitsReply() {
        return getDeliveryStatus() == DeliveryStatus.SENT && attendanceReply == AttendanceReply.NO_REPLY;
    }

    public ConvocationId getId() {
        return id;
    }

    public GeneralMeetingId getGeneralMeetingId() {
        return generalMeetingId;
    }

    public EntityId getUnitId() {
        return unitId;
    }

    public VotingWeight getVotingWeight() {
        return votingWeight;
    }

    /** Every attempt, in the order they were recorded. Unmodifiable. */
    public List<ConvocationDelivery> getDeliveries() {
        return deliveries;
    }

    /**
     * The secret behind this convocation's confirmation link. Never exposed by
     * the back-office read models: it travels in the email and on the printed
     * letter, and a tracking table returning one token per lot would hand every
     * lot's answer to whoever can read that page.
     */
    public String getConfirmationToken() {
        return confirmationToken;
    }

    public AttendanceReply getAttendanceReply() {
        return attendanceReply;
    }

    public Instant getRepliedAt() {
        return repliedAt;
    }

    public ReplySource getReplySource() {
        return replySource;
    }

    public EntityId getRepliedByPartyId() {
        return repliedByPartyId;
    }

    public String getReplyNote() {
        return replyNote;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public AttendanceMode getAttendanceMode() {
        return attendanceMode;
    }

    public EntityId getCheckedInPartyId() {
        return checkedInPartyId;
    }

    public Instant getCheckedInAt() {
        return checkedInAt;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Convocation other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
