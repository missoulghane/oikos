package com.architek.oikos.meeting.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.meeting.domain.exception.InvalidMinutesStatusTransitionException;
import com.architek.oikos.meeting.domain.exception.MinutesLockedException;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingMinutesId;
import com.architek.oikos.meeting.domain.valueobject.MinutesStatus;

/**
 * The record of one general meeting - one per meeting, and the place where
 * everything this module computes finally stops being computed.
 *
 * <p>That is the whole point of {@code content}. Attendance, tallies and
 * outcomes are derived on every read everywhere else, so that they can never
 * disagree with the rows they come from; but a procès-verbal has to say what
 * was decided on the day, not what the current data would conclude. Generating
 * the draft renders those figures into text once, and from there the text is
 * the truth. A lot sold, a tantième corrected or a majority rule reinterpreted
 * afterwards changes nothing in a meeting already minuted.
 *
 * <p>content is HTML: the minutes are edited in a rich-text editor and printed
 * to PDF, and both need structure. Nothing else in this module stores markup.
 *
 * <p>Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based.
 */
public final class MeetingMinutes {

    private final MeetingMinutesId id;
    private final GeneralMeetingId generalMeetingId;
    private final String content;
    private final MinutesStatus status;
    private final Instant publishedAt;
    private final Instant createdDate;

    private MeetingMinutes(MeetingMinutesId id, GeneralMeetingId generalMeetingId, String content,
                            MinutesStatus status, Instant publishedAt, Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.generalMeetingId = Objects.requireNonNull(generalMeetingId, "generalMeetingId must not be null");
        this.content = requireContent(content);
        this.status = Objects.requireNonNull(status, "status must not be null");
        if (status == MinutesStatus.PUBLISHED && publishedAt == null) {
            throw new IllegalArgumentException("published minutes must carry a publication date");
        }
        this.publishedAt = publishedAt;
        this.createdDate = createdDate;
    }

    public static MeetingMinutes draft(MeetingMinutesId id, GeneralMeetingId generalMeetingId, String content) {
        return new MeetingMinutes(id, generalMeetingId, content, MinutesStatus.DRAFT, null, null);
    }

    public static MeetingMinutes reconstruct(MeetingMinutesId id, GeneralMeetingId generalMeetingId, String content,
                                              MinutesStatus status, Instant publishedAt, Instant createdDate) {
        return new MeetingMinutes(id, generalMeetingId, content, status, publishedAt, createdDate);
    }

    /**
     * Completing or correcting the draft before validation - the syndic adds
     * what the figures cannot say: debates, remarks, an opposition a
     * copropriétaire asked to have recorded.
     */
    public MeetingMinutes withContent(String newContent) {
        requireEditable();
        return new MeetingMinutes(id, generalMeetingId, newContent, status, publishedAt, createdDate);
    }

    /**
     * Regenerating the draft from the session's data, discarding manual edits.
     * Separate from withContent because it is not the same act: one is the
     * syndic writing, the other is starting over.
     */
    public MeetingMinutes regeneratedWith(String newContent) {
        requireEditable();
        return new MeetingMinutes(id, generalMeetingId, newContent, MinutesStatus.DRAFT, null, createdDate);
    }

    /** DRAFT -&gt; UNDER_REVIEW: the text is frozen, only publication may follow. */
    public MeetingMinutes validate() {
        requireTransitionTo(MinutesStatus.UNDER_REVIEW);
        return new MeetingMinutes(id, generalMeetingId, content, MinutesStatus.UNDER_REVIEW, null, createdDate);
    }

    /** UNDER_REVIEW -&gt; PUBLISHED: diffused to the copropriétaires, and final. */
    public MeetingMinutes publish(Instant at) {
        requireTransitionTo(MinutesStatus.PUBLISHED);
        return new MeetingMinutes(id, generalMeetingId, content, MinutesStatus.PUBLISHED,
                Objects.requireNonNull(at, "at must not be null"), createdDate);
    }

    public void requireEditable() {
        if (!status.isEditable()) {
            throw new MinutesLockedException(status);
        }
    }

    private void requireTransitionTo(MinutesStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidMinutesStatusTransitionException(status, target);
        }
    }

    private static String requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("minutes content must not be blank");
        }
        return content;
    }

    public MeetingMinutesId getId() {
        return id;
    }

    public GeneralMeetingId getGeneralMeetingId() {
        return generalMeetingId;
    }

    public String getContent() {
        return content;
    }

    public MinutesStatus getStatus() {
        return status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof MeetingMinutes other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
