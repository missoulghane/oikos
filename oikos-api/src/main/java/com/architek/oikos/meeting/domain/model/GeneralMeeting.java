package com.architek.oikos.meeting.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.meeting.domain.exception.InvalidMeetingStatusTransitionException;
import com.architek.oikos.meeting.domain.exception.QuorumNotReachedException;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A general meeting of one copropriété, and the six-state lifecycle of the
 * SFD. Every transition goes through this class; MeetingStatus owns the graph
 * of what may follow what.
 *
 * <p>propertyId is the generic {@link EntityId} rather than property's own
 * PropertyId, keeping this module decoupled from property's identity types
 * (same rationale as Invitation.propertyId) - resolved back to property's
 * types only at the adapter boundary (MeetingPropertyDirectoryAdapter).
 *
 * <p>quorumPercentage and votingWeightMode are SNAPSHOTS taken at creation:
 * the first from the property's MeetingQuorumSetting for this meeting type,
 * the second from its dues calculation mode. Re-reading them live would mean
 * a configuration change made months later silently restates how a meeting
 * already held was counted - the same reason an installment keeps the price
 * it was issued at (RG002).
 *
 * <p>The agenda is deliberately NOT held here. Items are a separate entity
 * with their own table and their own paging; loading the whole agenda to
 * rename a meeting would be a poor trade, and no invariant of this aggregate
 * spans both. The one rule that does - "at least one item before scheduling"
 * - is enforced by ScheduleGeneralMeetingService, which is the only place
 * that can count them without this class reaching into a repository.
 *
 * <p>createdDate mirrors the persistence layer's audited created_date
 * (read-only here - JPA auditing is its sole writer): null on a freshly
 * created instance, populated by the mapper once reloaded.
 *
 * <p>Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based.
 */
public final class GeneralMeeting {

    private static final int MAX_TITLE_LENGTH = 200;

    private final GeneralMeetingId id;
    private final EntityId propertyId;
    private final MeetingType meetingType;
    private final MeetingStatus status;
    private final String title;
    private final Instant scheduledAt;
    private final MeetingVenue venue;
    private final QuorumPercentage quorumPercentage;
    private final VotingWeightMode votingWeightMode;
    private final String comment;
    private final ShortCode publicReference;
    private final boolean openedWithoutQuorum;
    private final Instant createdDate;

    private GeneralMeeting(GeneralMeetingId id, EntityId propertyId, MeetingType meetingType, MeetingStatus status,
                            String title, Instant scheduledAt, MeetingVenue venue, QuorumPercentage quorumPercentage,
                            VotingWeightMode votingWeightMode, String comment, ShortCode publicReference,
                            boolean openedWithoutQuorum, Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.meetingType = Objects.requireNonNull(meetingType, "meetingType must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.title = requireValidTitle(title);
        this.quorumPercentage = Objects.requireNonNull(quorumPercentage, "quorumPercentage must not be null");
        this.votingWeightMode = Objects.requireNonNull(votingWeightMode, "votingWeightMode must not be null");
        if (status != MeetingStatus.DRAFT && (scheduledAt == null || venue == null)) {
            throw new IllegalArgumentException("a " + status + " meeting must have both a date and a venue");
        }
        this.scheduledAt = scheduledAt;
        this.venue = venue;
        this.comment = comment;
        this.publicReference = Objects.requireNonNull(publicReference, "publicReference must not be null");
        this.openedWithoutQuorum = openedWithoutQuorum;
        this.createdDate = createdDate;
    }

    /**
     * A draft may be created with or without a date and a venue - both stay
     * nullable until the meeting is scheduled, which is the transition that
     * makes them mandatory. Callers that know them (the web form asks for them
     * on the creation screen) pass them straight in rather than creating an
     * empty draft and immediately updating it.
     */
    public static GeneralMeeting createDraft(GeneralMeetingId id, EntityId propertyId, MeetingType meetingType,
                                              String title, Instant scheduledAt, MeetingVenue venue,
                                              QuorumPercentage quorumPercentage, VotingWeightMode votingWeightMode,
                                              ShortCode publicReference) {
        return new GeneralMeeting(id, propertyId, meetingType, MeetingStatus.DRAFT, title, scheduledAt, venue,
                quorumPercentage, votingWeightMode, null, publicReference, false, null);
    }

    public static GeneralMeeting reconstruct(GeneralMeetingId id, EntityId propertyId, MeetingType meetingType,
                                              MeetingStatus status, String title, Instant scheduledAt, MeetingVenue venue,
                                              QuorumPercentage quorumPercentage, VotingWeightMode votingWeightMode,
                                              String comment, ShortCode publicReference, boolean openedWithoutQuorum,
                                              Instant createdDate) {
        return new GeneralMeeting(id, propertyId, meetingType, status, title, scheduledAt, venue, quorumPercentage,
                votingWeightMode, comment, publicReference, openedWithoutQuorum, createdDate);
    }

    /**
     * Editing the meeting's own fields, at any point of its lifecycle.
     *
     * <p>Deliberately unguarded (decision of 2026-08-16, ADR 0002 §8). The
     * strict reading is that a date and a venue freeze once the convocations
     * go out - owners have been told where to turn up - and the code enforced
     * exactly that until now. The product decision is to carry no functional
     * blocking rule for the time being: a syndic correcting a typo in an
     * address must not have to delete the assembly and start again. The
     * constraint is deferred, not forgotten - see the ADR.
     *
     * <p>The invariant that a non-DRAFT meeting cannot lose its date or venue
     * altogether still holds: it lives in the constructor, and it is
     * structural (the DB says the same), not a business rule.
     */
    public GeneralMeeting update(MeetingType newMeetingType, String newTitle, Instant newScheduledAt,
                                  MeetingVenue newVenue) {
        return new GeneralMeeting(id, propertyId, newMeetingType, status, newTitle, newScheduledAt, newVenue,
                quorumPercentage, votingWeightMode, comment, publicReference, openedWithoutQuorum, createdDate);
    }

    /**
     * The syndic's note of intent, in rich text, read by the copropriétaires.
     *
     * <p>Its own verb rather than one more parameter on {@link #update}: it is
     * edited on its own screen, and routing it through the full update would
     * make "save a comment" capable of overwriting the date and the venue with
     * whatever the screen happened to be holding.
     *
     * <p>Unguarded by status, like update() and for the same reason (ADR 0002
     * §8) - and here the case for it is stronger still: a syndic adding a
     * clarification the day before the session is doing their job.
     *
     * <p>Blank becomes null. An empty editor and no comment at all say the same
     * thing, and letting both exist means every reader has to check for both.
     */
    public GeneralMeeting withComment(String newComment) {
        String normalized = newComment == null || newComment.isBlank() ? null : newComment;
        return new GeneralMeeting(id, propertyId, meetingType, status, title, scheduledAt, venue, quorumPercentage,
                votingWeightMode, normalized, publicReference, openedWithoutQuorum, createdDate);
    }

    /**
     * DRAFT -&gt; SCHEDULED: the meeting is ready to convoke. It no longer
     * freezes the agenda - that rule is deferred (ADR 0002 §8).
     */
    public GeneralMeeting schedule(Instant newScheduledAt, MeetingVenue newVenue) {
        requireTransitionTo(MeetingStatus.SCHEDULED);
        Objects.requireNonNull(newScheduledAt, "scheduledAt must not be null");
        Objects.requireNonNull(newVenue, "venue must not be null");
        return withStatus(MeetingStatus.SCHEDULED, newScheduledAt, newVenue, openedWithoutQuorum);
    }

    /** SCHEDULED -&gt; CONVENED, once the convocations exist (lot 3). */
    public GeneralMeeting convene() {
        requireTransitionTo(MeetingStatus.CONVENED);
        return withStatus(MeetingStatus.CONVENED, scheduledAt, venue, openedWithoutQuorum);
    }

    /**
     * CONVENED -&gt; IN_PROGRESS. Refuses to open without quorum unless the
     * caller says so explicitly, and records that it was forced - opening
     * anyway is lawful but consequential, and the minutes have to say it
     * happened (ADR 0002 §5).
     */
    public GeneralMeeting open(boolean quorumReached, boolean forceWithoutQuorum) {
        requireTransitionTo(MeetingStatus.IN_PROGRESS);
        if (!quorumReached && !forceWithoutQuorum) {
            throw new QuorumNotReachedException();
        }
        return withStatus(MeetingStatus.IN_PROGRESS, scheduledAt, venue, !quorumReached);
    }

    /** IN_PROGRESS -&gt; CLOSED. */
    public GeneralMeeting close() {
        requireTransitionTo(MeetingStatus.CLOSED);
        return withStatus(MeetingStatus.CLOSED, scheduledAt, venue, openedWithoutQuorum);
    }

    /** CLOSED -&gt; MINUTES_PUBLISHED, driven by the minutes being published (lot 5). */
    public GeneralMeeting markMinutesPublished() {
        requireTransitionTo(MeetingStatus.MINUTES_PUBLISHED);
        return withStatus(MeetingStatus.MINUTES_PUBLISHED, scheduledAt, venue, openedWithoutQuorum);
    }

    private GeneralMeeting withStatus(MeetingStatus newStatus, Instant newScheduledAt, MeetingVenue newVenue,
                                       boolean newOpenedWithoutQuorum) {
        return new GeneralMeeting(id, propertyId, meetingType, newStatus, title, newScheduledAt, newVenue,
                quorumPercentage, votingWeightMode, comment, publicReference, newOpenedWithoutQuorum, createdDate);
    }

    private void requireTransitionTo(MeetingStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidMeetingStatusTransitionException(status, target);
        }
    }

    private static String requireValidTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        String trimmed = title.trim();
        if (trimmed.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("title must be at most " + MAX_TITLE_LENGTH + " characters");
        }
        return trimmed;
    }

    public GeneralMeetingId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public MeetingType getMeetingType() {
        return meetingType;
    }

    public MeetingStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public MeetingVenue getVenue() {
        return venue;
    }

    public QuorumPercentage getQuorumPercentage() {
        return quorumPercentage;
    }

    /**
     * Rich-text HTML, produced by an editor and read by copropriétaires - so it
     * is never re-injected raw anywhere. The clients sanitize before display and
     * the convocation PDF takes its plain text (ADR 0002 §12).
     */
    public String getComment() {
        return comment;
    }

    /**
     * What a copropriétaire types to say which assembly their code belongs to.
     * Public by construction - it is printed next to the code it scopes, and it
     * protects nothing on its own. What it does is bound the search space for a
     * guessed code to the lots of one copropriété (ADR 0002 §13).
     */
    public ShortCode getPublicReference() {
        return publicReference;
    }

    public VotingWeightMode getVotingWeightMode() {
        return votingWeightMode;
    }

    public boolean isOpenedWithoutQuorum() {
        return openedWithoutQuorum;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof GeneralMeeting other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
